# Shared implementation for the Windows local launcher; .env is data, never PowerShell code.
Set-StrictMode -Version Latest

function Get-BackendPaths {
    $repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
    $runtime = Join-Path $repository 'var/local-runtime'
    [IO.Directory]::CreateDirectory($runtime) | Out-Null
    return @{
        Repository = $repository
        Runtime = $runtime
        Jar = Join-Path $repository 'apps/api/target/contract-governance-api-0.1.0-SNAPSHOT.jar'
        Env = Join-Path $repository '.env'
        ProcessMetadata = Join-Path $runtime 'api-process.json'
        ComposeMetadata = Join-Path $runtime 'compose-project.json'
        ComposeOverride = Join-Path $runtime 'compose.override.yml'
        ComposeEnv = Join-Path $runtime 'compose.env'
    }
}

function Enter-BackendLock([hashtable] $Paths) {
    try {
        return [IO.File]::Open((Join-Path $Paths.Runtime 'launcher.lock'),
            [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
    } catch {
        throw 'Another start/stop operation is using this repository. Wait for it to finish.'
    }
}

function Read-BackendEnv([string] $Path) {
    $allowed = @('CG_DATABASE_PASSWORD', 'CG_CURSOR_SECRET', 'CG_DEMO_PASSWORD',
        'SPRING_PROFILES_ACTIVE', 'CG_STORAGE_ROOT')
    $values = @{}
    if (-not [IO.File]::Exists($Path)) { return $values }
    $lineNumber = 0
    foreach ($line in [IO.File]::ReadAllLines($Path)) {
        $lineNumber++
        if ($line -match '^\s*(#.*)?$') { continue }
        if ($line -notmatch '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=(.*)$') {
            throw ".env line $lineNumber must be KEY=literal; shell statements are not accepted."
        }
        $key = $Matches[1]
        $value = $Matches[2].Trim()
        if ($allowed -notcontains $key) { continue }
        if ($values.ContainsKey($key)) { throw ".env repeats $key at line $lineNumber." }
        if ($value.StartsWith('"') -or $value.StartsWith("'")) {
            $quote = $value.Substring(0, 1)
            if ($value.Length -lt 2 -or -not $value.EndsWith($quote)) {
                throw ".env has an unmatched quote for $key at line $lineNumber."
            }
            $value = $value.Substring(1, $value.Length - 2)
        }
        # No expansion of $, backticks, backslashes or commands; inline # is part of the value.
        $values[$key] = $value
    }
    return $values
}

function Get-BackendSetting([hashtable] $Values, [string] $Key) {
    $inherited = [Environment]::GetEnvironmentVariable($Key, 'Process')
    if ($null -ne $inherited) { return $inherited }
    if ($Values.ContainsKey($Key)) { return [string] $Values[$Key] }
    return $null
}

function New-BackendSecret([int] $ByteCount) {
    $bytes = New-Object byte[] $ByteCount
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $random.GetBytes($bytes) } finally { $random.Dispose() }
    return [Convert]::ToBase64String($bytes)
}

function Initialize-BackendSecrets([hashtable] $Paths, [hashtable] $Values, [bool] $Demo) {
    $keys = @('CG_DATABASE_PASSWORD', 'CG_CURSOR_SECRET')
    if ($Demo) { $keys += 'CG_DEMO_PASSWORD' }
    foreach ($key in $keys) {
        if ($null -ne (Get-BackendSetting $Values $key)) { continue }
        $bytes = if ($key -eq 'CG_CURSOR_SECRET') { 48 } else { 24 }
        $value = New-BackendSecret $bytes
        # Append only absent keys. Never rotate an existing password or rewrite the user's .env.
        [IO.File]::AppendAllText($Paths.Env, "`r`n$key=$value`r`n", (New-Object Text.UTF8Encoding $false))
        $Values[$key] = $value
    }
}

function Protect-BackendOutput([string] $Text, [string[]] $Secrets) {
    foreach ($secret in $Secrets) {
        if (-not [string]::IsNullOrEmpty($secret)) { $Text = $Text.Replace($secret, '[redacted]') }
    }
    return [regex]::Replace($Text,
        '(?im)(password|secret|token)\s*[:=]\s*[^\r\n]+', '$1=[redacted]')
}

function ConvertTo-BackendNativeArgument([string] $Argument) {
    if ($Argument.Length -gt 0 -and $Argument -notmatch '[\s"]') { return $Argument }
    # Windows CommandLineToArgvW quoting, including quotes and trailing backslashes.
    return '"' + (($Argument -replace '(\\*)"', '$1$1\"') -replace '(\\+)$', '$1$1') + '"'
}

function Invoke-BackendNative {
    param([string] $Executable, [string[]] $Arguments, [hashtable] $Paths,
        [string] $Name, [int] $TimeoutSeconds, [string[]] $Secrets = @(), [switch] $CaptureStandardError)
    $id = [Guid]::NewGuid().ToString('N')
    $stdout = Join-Path $Paths.Runtime "$Name-$id.stdout.log"
    $stderr = Join-Path $Paths.Runtime "$Name-$id.stderr.log"
    $quoted = @($Arguments | ForEach-Object { ConvertTo-BackendNativeArgument $_ })
    $child = Start-Process -FilePath $Executable -ArgumentList $quoted -WorkingDirectory $Paths.Repository `
        -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru
    try {
        if (-not $child.WaitForExit($TimeoutSeconds * 1000)) {
            $child.Kill()
            $null = $child.WaitForExit(5000)
            throw "$Name exceeded ${TimeoutSeconds}s. Logs: $stdout and $stderr."
        }
        $output = [IO.File]::ReadAllText($stdout)
        if ($child.ExitCode -ne 0) {
            $tail = ([IO.File]::ReadAllLines($stderr) | Select-Object -Last 12) -join "`n"
            throw "$Name failed (exit $($child.ExitCode)). $(Protect-BackendOutput $tail $Secrets)"
        }
        if ($CaptureStandardError) { $output += [IO.File]::ReadAllText($stderr) }
        return $output.Trim()
    } finally { $child.Dispose() }
}

function Get-BackendRemainingSeconds([datetime] $Deadline, [int] $Maximum = 120) {
    $remaining = [int] [Math]::Ceiling(($Deadline - [datetime]::UtcNow).TotalSeconds)
    if ($remaining -le 0) { throw 'Local startup readiness deadline expired.' }
    return [Math]::Min($remaining, $Maximum)
}

function Get-BackendComposeArguments([hashtable] $Paths, [string] $ProjectName) {
    return @('compose', '--project-name', $ProjectName, '--project-directory', $Paths.Repository,
        '--env-file', $Paths.ComposeEnv, '-f', (Join-Path $Paths.Repository 'compose.yml'),
        '-f', $Paths.ComposeOverride)
}

function Assert-BackendComposeOwnership {
    param([string] $Docker, [hashtable] $Paths, [string] $ProjectName, [datetime] $Deadline,
        [string[]] $Secrets = @())
    $ids = Invoke-BackendNative $Docker @('ps', '-a', '--filter', "label=com.docker.compose.project=$ProjectName",
        '--format', '{{.ID}}') $Paths 'compose-ownership' (Get-BackendRemainingSeconds $Deadline 10) $Secrets
    $ownedContainerExists = $false
    foreach ($containerId in ($ids -split '\r?\n')) {
        if ([string]::IsNullOrWhiteSpace($containerId)) { continue }
        $owner = Invoke-BackendNative $Docker @('inspect', '--format',
            '{{index .Config.Labels "com.docker.compose.project.working_dir"}}', $containerId) `
            $Paths 'compose-owner' (Get-BackendRemainingSeconds $Deadline 10) $Secrets
        if ([string]::IsNullOrWhiteSpace($owner) -or
            -not [string]::Equals([IO.Path]::GetFullPath($owner), $Paths.Repository, [StringComparison]::OrdinalIgnoreCase)) {
            throw "Compose project '$ProjectName' belongs to another directory. Use a different -ComposeProjectName."
        }
        $ownedContainerExists = $true
    }
    $volumes = Invoke-BackendNative $Docker @('volume', 'ls', '--filter', "label=com.docker.compose.project=$ProjectName",
        '--format', '{{.Name}}') $Paths 'compose-volume-ownership' (Get-BackendRemainingSeconds $Deadline 10) $Secrets
    # An unlabeled volume with the exact managed name can also be reused by Compose.
    $namedVolumes = Invoke-BackendNative $Docker @('volume', 'ls', '--filter', "name=$ProjectName",
        '--format', '{{.Name}}') $Paths 'compose-volume-names' (Get-BackendRemainingSeconds $Deadline 10) $Secrets
    $collisions = @($namedVolumes -split '\r?\n' | Where-Object {
        $_ -ceq "${ProjectName}_postgres_data" -or $_ -ceq "${ProjectName}_scanner_signatures"
    })
    if ((-not [string]::IsNullOrWhiteSpace($volumes) -or $collisions.Count -gt 0) -and -not $ownedContainerExists) {
        $prior = Read-BackendMetadata $Paths.ComposeMetadata
        if ($null -eq $prior -or $prior.schemaVersion -ne 1 -or $prior.projectName -ne $ProjectName -or
            -not [string]::Equals($prior.repositoryPath, $Paths.Repository, [StringComparison]::OrdinalIgnoreCase)) {
            throw "Project '$ProjectName' has retained volumes without proven repository ownership. Use a different -ComposeProjectName; no volumes were changed."
        }
    }
}

function Read-BackendMetadata([string] $Path) {
    if (-not [IO.File]::Exists($Path)) { return $null }
    try { return [IO.File]::ReadAllText($Path) | ConvertFrom-Json } catch {
        throw "Invalid launcher metadata: $Path. No process was stopped."
    }
}

function Write-BackendMetadata([string] $Path, $Value) {
    [IO.File]::WriteAllText($Path, ($Value | ConvertTo-Json -Depth 5), (New-Object Text.UTF8Encoding $false))
}

function Get-ManagedBackendProcess([hashtable] $Paths, $Metadata) {
    if ($null -eq $Metadata) { return $null }
    if ($Metadata.schemaVersion -ne 1 -or $Metadata.processId -le 0 -or
        -not [string]::Equals($Metadata.repositoryPath, $Paths.Repository, [StringComparison]::OrdinalIgnoreCase) -or
        -not [string]::Equals($Metadata.jarPath, $Paths.Jar, [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Launcher process metadata does not identify this repository and jar. No process was stopped.'
    }
    $process = Get-Process -Id $Metadata.processId -ErrorAction SilentlyContinue
    if ($null -eq $process) { return $null }
    # Retain the process handle so PID reuse after validation cannot redirect Kill() to a new process.
    $null = $process.Handle
    $native = Get-CimInstance Win32_Process -Filter "ProcessId=$($Metadata.processId)"
    $jarArgument = [regex]::Match([string] $native.CommandLine, '(?:^|\s)-jar\s+(?:"(?<quoted>[^"]+)"|(?<bare>\S+))')
    $actualJar = if ($jarArgument.Groups['quoted'].Success) { $jarArgument.Groups['quoted'].Value } else { $jarArgument.Groups['bare'].Value }
    if (-not $jarArgument.Success -or [string]::IsNullOrWhiteSpace($native.ExecutablePath) -or
        -not [string]::Equals($native.ExecutablePath, $Metadata.javaPath, [StringComparison]::OrdinalIgnoreCase) -or
        -not [string]::Equals([IO.Path]::GetFullPath($actualJar), $Paths.Jar, [StringComparison]::OrdinalIgnoreCase) -or
        $process.StartTime.ToUniversalTime().Ticks.ToString() -ne $Metadata.startTimeUtcTicks) {
        $process.Dispose()
        throw 'PID identity differs from the recorded Java process. No process was reused or stopped.'
    }
    return $process
}

function Assert-BackendApiPort([int] $Port, [int] $ProcessId) {
    $listeners = @(Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
    return @($listeners | Where-Object { $_.OwningProcess -eq $ProcessId -and $_.LocalAddress -eq '127.0.0.1' }).Count -gt 0
}

function Show-BackendFailureLogs([string[]] $LogPaths, [string[]] $Secrets) {
    foreach ($log in $LogPaths) {
        if ([IO.File]::Exists($log)) {
            Write-Host "Startup log: $log"
            $tail = ([IO.File]::ReadAllLines($log) | Select-Object -Last 25) -join "`n"
            Write-Host (Protect-BackendOutput $tail $Secrets)
        }
    }
}
