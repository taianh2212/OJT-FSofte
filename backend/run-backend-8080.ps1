$port = 8080

# Find PID(s) listening on 8080 and stop them first.
$pids = netstat -ano | Select-String ":$port\s+.*LISTENING\s+(\d+)$" | ForEach-Object {
    if ($_ -match "(\d+)$") { [int]$matches[1] }
} | Sort-Object -Unique

if ($pids -and $pids.Count -gt 0) {
    Write-Host "Stopping process(es) on port ${port}: $($pids -join ', ')"
    foreach ($procId in $pids) {
        try {
            Stop-Process -Id $procId -Force -ErrorAction Stop
        } catch {
            Write-Warning "Could not stop PID ${procId}: $($_.Exception.Message)"
        }
    }
} else {
    Write-Host "Port $port is free."
}

Write-Host "Starting Spring Boot on port $port..."
mvn spring-boot:run
