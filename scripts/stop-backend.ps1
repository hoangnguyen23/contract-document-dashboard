#requires -Version 5.1
[CmdletBinding()]
param([switch] $StopServices)

$ErrorActionPreference = 'Stop'
if ([Environment]::OSVersion.Platform -ne [PlatformID]::Win32NT) { throw 'Use this launcher on Windows.' }
. (Join-Path $PSScriptRoot 'backend-runtime.ps1')
$paths = Get-BackendPaths
$lock = Enter-BackendLock $paths
try {
    $metadata = Read-BackendMetadata $paths.ProcessMetadata
    $process = Get-ManagedBackendProcess $paths $metadata
    if ($null -ne $process) {
        try {
            $process.Kill()
            if (-not $process.WaitForExit(15000)) { throw 'The managed API has not exited; its metadata is retained.' }
            Write-Host "Stopped the managed API (PID $($process.Id))."
        } finally { $process.Dispose() }
    } else { Write-Host 'No managed API process is running.' }
    if ([IO.File]::Exists($paths.ProcessMetadata)) { [IO.File]::Delete($paths.ProcessMetadata) }
    if ($StopServices) {
        $compose = Read-BackendMetadata $paths.ComposeMetadata
        if ($null -eq $compose -or $compose.schemaVersion -ne 1 -or
            -not [string]::Equals($compose.repositoryPath, $paths.Repository, [StringComparison]::OrdinalIgnoreCase) -or
            $compose.projectName -notmatch '^[a-z0-9][a-z0-9_-]*$') {
            throw 'No valid Compose ownership metadata exists for this repository. No containers were stopped.'
        }
        $docker = (Get-Command docker.exe -CommandType Application | Select-Object -First 1).Source
        Assert-BackendComposeOwnership $docker $paths $compose.projectName ([datetime]::UtcNow.AddSeconds(45))
        $null = Invoke-BackendNative $docker ((Get-BackendComposeArguments $paths $compose.projectName) + @('stop', 'postgres', 'scanner')) `
            $paths 'compose-stop' 45
        Write-Host 'Stopped this project PostgreSQL and ClamAV containers; volumes and private files are retained.'
    } else { Write-Host 'PostgreSQL and ClamAV remain running. Use -StopServices to stop this project containers without removing data.' }
} finally { $lock.Dispose() }
