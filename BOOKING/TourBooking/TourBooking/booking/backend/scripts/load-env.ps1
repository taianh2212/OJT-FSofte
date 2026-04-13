param(
    [string]$EnvFile = ".env"
)

if (!(Test-Path $EnvFile)) {
    Write-Error "Cannot find $EnvFile. Copy .env.example to .env first."
    exit 1
}

Get-Content $EnvFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) { return }
    $idx = $line.IndexOf("=")
    if ($idx -lt 1) { return }
    $name = $line.Substring(0, $idx).Trim()
    $value = $line.Substring($idx + 1).Trim()
    [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
}

Write-Host "Loaded environment variables from $EnvFile into current PowerShell process."
Write-Host "Now run: mvn spring-boot:run"
