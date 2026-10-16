#requires -Version 5.1
[CmdletBinding()]
param(
    [ValidateRange(1024, 65535)] [int] $DatabasePort = 55432,
    [ValidateRange(1024, 65535)] [int] $ApiPort = 8081,
    [ValidateRange(1024, 65535)] [int] $ScannerPort = 3310,
    [ValidatePattern('^[a-z0-9][a-z0-9_-]*$')] [string] $ComposeProjectName = 'contract-governance',
    [ValidateRange(30, 1800)] [int] $StartupTimeoutSeconds = 240,
    [switch] $Demo,
    [switch] $SkipBuild
)

$ErrorActionPreference = 'Stop'
if ([Environment]::OSVersion.Platform -ne [PlatformID]::Win32NT) { throw 'Use this launcher on Windows.' }
. (Join-Path $PSScriptRoot 'backend-runtime.ps1')
$paths = Get-BackendPaths
$lock = Enter-BackendLock $paths
$previousEnvironment = @{}
$secrets = @()
$apiLogs = @()
$startedProcess = $null
try {
    if (@($DatabasePort, $ApiPort, $ScannerPort | Select-Object -Unique).Count -ne 3) {
        throw 'DatabasePort, ApiPort and ScannerPort must differ.'
    }
    $values = Read-BackendEnv $paths.Env
    Initialize-BackendSecrets $paths $values $Demo.IsPresent
    $databasePassword = Get-BackendSetting $values 'CG_DATABASE_PASSWORD'
    $cursorSecret = Get-BackendSetting $values 'CG_CURSOR_SECRET'
    $demoPassword = Get-BackendSetting $values 'CG_DEMO_PASSWORD'
    $secrets = @($databasePassword, $cursorSecret, $demoPassword)
    if ([string]::IsNullOrWhiteSpace($databasePassword)) { throw 'CG_DATABASE_PASSWORD must not be empty.' }
    if ([string]::IsNullOrWhiteSpace($cursorSecret) -or $cursorSecret.Length -lt 32 -or $cursorSecret.StartsWith('replace-with-')) {
        throw 'Supply a random CG_CURSOR_SECRET of at least 32 characters; an existing .env value is never replaced.'
    }
    $profile = Get-BackendSetting $values 'SPRING_PROFILES_ACTIVE'
    if ($Demo) {
        if (-not [string]::IsNullOrWhiteSpace($profile) -and $profile -ne 'demo') {
            throw '-Demo conflicts with the configured SPRING_PROFILES_ACTIVE. Choose the profile explicitly.'
        }
        $profile = 'demo'
    }
    if (($profile -split ',' | ForEach-Object { $_.Trim() }) -contains 'demo') {
        if ([string]::IsNullOrEmpty($demoPassword) -or $demoPassword.Length -lt 12 -or
            $demoPassword.Length -gt 128 -or $demoPassword.StartsWith('replace-with-')) {
            throw 'The demo profile requires CG_DEMO_PASSWORD (12-128 characters); replace any placeholder privately.'
        }
    }
    $storage = Get-BackendSetting $values 'CG_STORAGE_ROOT'
    if ([string]::IsNullOrWhiteSpace($storage)) { $storage = Join-Path $paths.Repository 'var/private-files' }
    if (-not [IO.Path]::IsPathRooted($storage)) { $storage = Join-Path $paths.Repository $storage }
    $storage = [IO.Path]::GetFullPath($storage)
    [IO.Directory]::CreateDirectory($storage) | Out-Null

    $metadata = Read-BackendMetadata $paths.ProcessMetadata
    $managedProcess = Get-ManagedBackendProcess $paths $metadata
    if ($null -ne $managedProcess) {
        try {
            if ($metadata.apiPort -ne $ApiPort -or $metadata.databasePort -ne $DatabasePort -or
                $metadata.scannerPort -ne $ScannerPort -or $metadata.composeProjectName -ne $ComposeProjectName -or
                $metadata.profile -ne [string] $profile -or $metadata.storagePath -ne $storage -or
                $metadata.jarSha256 -ne (Get-FileHash -LiteralPath $paths.Jar -Algorithm SHA256).Hash -or
                -not (Assert-BackendApiPort $ApiPort $managedProcess.Id)) {
                throw 'The managed API is running with different inputs or is not ready. Stop it with scripts/stop-backend.ps1 first.'
            }
            Write-Host "Managed API is already listening on http://127.0.0.1:$ApiPort (PID $($managedProcess.Id))."
            Write-Host 'Changed secrets/environment require stop then start. Browser authentication still requires a same-origin HTTPS proxy.'
            return
        } finally { $managedProcess.Dispose() }
    }
    $listener = New-Object Net.Sockets.TcpListener ([Net.IPAddress]::Loopback), $ApiPort
    try { $listener.Start() } catch { throw "API port $ApiPort is already occupied. Choose -ApiPort; no other process will be stopped." }
    finally { $listener.Stop() }

    $java = (Get-Command java.exe -CommandType Application | Select-Object -First 1).Source
    $docker = (Get-Command docker.exe -CommandType Application | Select-Object -First 1).Source
    $javaVersion = Invoke-BackendNative $java @('-version') $paths 'java-version' 10 $secrets -CaptureStandardError
    if ($javaVersion -notmatch '(?:java|openjdk) version "25(?:\.|\")') {
        throw 'A Java 25 JDK must be available as java.exe on PATH.'
    }
    if (-not $SkipBuild) {
        Push-Location (Join-Path $paths.Repository 'apps/api')
        try {
            & .\mvnw.cmd '-Dmaven.test.skip=true' package
            if ($LASTEXITCODE -ne 0) { throw 'Backend package failed; tests were skipped.' }
        } finally { Pop-Location }
    }
    if (-not [IO.File]::Exists($paths.Jar)) { throw 'Packaged API jar is missing. Run again without -SkipBuild.' }

    $environment = @{
        CG_DATABASE_PASSWORD = $databasePassword
        CG_CURSOR_SECRET = $cursorSecret
        CG_DATABASE_URL = "jdbc:postgresql://127.0.0.1:$DatabasePort/contract_governance"
        CG_DATABASE_USER = 'contract_governance'
        CG_SCANNER_HOST = '127.0.0.1'
        CG_SCANNER_PORT = [string] $ScannerPort
        CG_SERVER_ADDRESS = '127.0.0.1'
        CG_STORAGE_ROOT = $storage
        PORT = [string] $ApiPort
        SPRING_PROFILES_ACTIVE = [string] $profile
        CG_DEMO_PASSWORD = $demoPassword
    }
    foreach ($key in $environment.Keys) {
        $previousEnvironment[$key] = [Environment]::GetEnvironmentVariable($key, 'Process')
        [Environment]::SetEnvironmentVariable($key, $environment[$key], 'Process')
    }
    $deadline = [datetime]::UtcNow.AddSeconds($StartupTimeoutSeconds)
    $composeVersion = Invoke-BackendNative $docker @('compose', 'version', '--short') $paths 'compose-version' 10 $secrets
    if ($composeVersion -notmatch '(\d+)\.(\d+)\.(\d+)' -or [version] $Matches[0] -lt [version] '2.24.4') {
        throw 'Docker Compose >= 2.24.4 is required for !override port replacement.'
    }
    $null = Invoke-BackendNative $docker @('info', '--format', '{{.ServerVersion}}') $paths 'docker-engine' 10 $secrets
    Assert-BackendComposeOwnership $docker $paths $ComposeProjectName $deadline $secrets
    $override = @"
services:
  postgres:
    ports: !override
      - "127.0.0.1:${DatabasePort}:5432"
  scanner:
    ports: !override
      - "127.0.0.1:${ScannerPort}:3310"
"@
    [IO.File]::WriteAllText($paths.ComposeOverride, $override, (New-Object Text.UTF8Encoding $false))
    # Prevent Compose from rereading/interpolating the user's .env; only allowlisted process variables are supplied.
    [IO.File]::WriteAllText($paths.ComposeEnv, "# Values are supplied through the launcher process environment.`n", (New-Object Text.UTF8Encoding $false))
    Write-BackendMetadata $paths.ComposeMetadata @{
        schemaVersion = 1; repositoryPath = $paths.Repository; projectName = $ComposeProjectName
    }
    $composeArguments = Get-BackendComposeArguments $paths $ComposeProjectName
    Write-Host "Starting local PostgreSQL on 127.0.0.1:$DatabasePort and ClamAV on 127.0.0.1:$ScannerPort..."
    $null = Invoke-BackendNative $docker ($composeArguments + @('up', '-d', 'postgres', 'scanner')) `
        $paths 'compose-up' (Get-BackendRemainingSeconds $deadline) $secrets
    foreach ($service in @('postgres', 'scanner')) {
        $containerId = Invoke-BackendNative $docker ($composeArguments + @('ps', '-q', $service)) `
            $paths "container-$service" (Get-BackendRemainingSeconds $deadline 10) $secrets
        if ([string]::IsNullOrWhiteSpace($containerId) -or $containerId -match '\s') { throw "$service did not create exactly one local container." }
        do {
            $state = Invoke-BackendNative $docker @('inspect', '--format',
                '{{.State.Status}}|{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}', $containerId) `
                $paths "readiness-$service" (Get-BackendRemainingSeconds $deadline 10) $secrets
            if ($state -eq 'running|healthy') { break }
            if ($state -match '^(exited|dead)\|' -or $state -eq 'running|unhealthy' -or $state -eq 'running|none') {
                throw "$service is $state. Review its local container logs; startup is not ready."
            }
            Start-Sleep -Milliseconds 1000
        } while ($true)
    }

    $id = [Guid]::NewGuid().ToString('N')
    $stdout = Join-Path $paths.Runtime "api-$id.stdout.log"
    $stderr = Join-Path $paths.Runtime "api-$id.stderr.log"
    $apiLogs = @($stdout, $stderr)
    $arguments = @('-Duser.timezone=UTC', '-jar', (ConvertTo-BackendNativeArgument $paths.Jar))
    $startedProcess = Start-Process -FilePath $java -ArgumentList $arguments -WorkingDirectory $paths.Repository `
        -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru
    $null = $startedProcess.Handle
    $actualJava = (Get-CimInstance Win32_Process -Filter "ProcessId=$($startedProcess.Id)").ExecutablePath
    if ([string]::IsNullOrWhiteSpace($actualJava)) { throw 'Cannot verify the new Java executable path.' }
    Write-BackendMetadata $paths.ProcessMetadata @{
        schemaVersion = 1; repositoryPath = $paths.Repository; jarPath = $paths.Jar; javaPath = $actualJava
        processId = $startedProcess.Id; startTimeUtcTicks = $startedProcess.StartTime.ToUniversalTime().Ticks.ToString()
        apiPort = $ApiPort; databasePort = $DatabasePort; scannerPort = $ScannerPort
        composeProjectName = $ComposeProjectName; profile = [string] $profile; storagePath = $storage
        jarSha256 = (Get-FileHash -LiteralPath $paths.Jar -Algorithm SHA256).Hash
        stdoutPath = $stdout; stderrPath = $stderr
    }
    do {
        $null = Get-BackendRemainingSeconds $deadline
        $startedProcess.Refresh()
        if ($startedProcess.HasExited) { throw "API exited during startup (exit $($startedProcess.ExitCode))." }
        $log = [IO.File]::ReadAllText($stdout)
        if ($log -match '(?m)\bCG_BACKEND_READY\s*$' -and (Assert-BackendApiPort $ApiPort $startedProcess.Id)) { break }
        Start-Sleep -Milliseconds 500
    } while ($true)
    Write-Host "API startup complete: http://127.0.0.1:$ApiPort (PID $($startedProcess.Id)); PostgreSQL/Flyway and container health are ready."
    Write-Host "Logs and process metadata: $($paths.Runtime)"
    Write-Host 'Browser authentication requires a same-origin HTTPS proxy. No HTTP requests or business acceptance scenarios were run.'
} catch {
    Show-BackendFailureLogs $apiLogs $secrets
    if ($null -ne $startedProcess -and -not $startedProcess.HasExited) {
        # Stop only the exact retained process handle created by this invocation.
        $startedProcess.Kill()
        $null = $startedProcess.WaitForExit(15000)
    }
    throw (Protect-BackendOutput $_.Exception.Message $secrets)
} finally {
    foreach ($key in $previousEnvironment.Keys) { [Environment]::SetEnvironmentVariable($key, $previousEnvironment[$key], 'Process') }
    if ($null -ne $startedProcess) { $startedProcess.Dispose() }
    $lock.Dispose()
}
