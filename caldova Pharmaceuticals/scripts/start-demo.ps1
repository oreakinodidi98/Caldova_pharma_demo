. (Join-Path $PSScriptRoot 'common.ps1')
& (Join-Path $PSScriptRoot 'setup-demo.ps1')
Invoke-Compose up -d --build app
$ready = $false
for ($attempt = 0; $attempt -lt 60; $attempt++) {
    try {
        $response = Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/health' -TimeoutSec 5
        if ($response.status -eq 'UP') { $ready = $true; break }
    } catch [System.Net.WebException] {
        if ($attempt -eq 59) { Write-Warning $_.Exception.Message }
    } catch [System.Net.Http.HttpRequestException] {
        if ($attempt -eq 59) { Write-Warning $_.Exception.Message }
    }
    Start-Sleep -Seconds 2
}
if (-not $ready) {
    Invoke-Compose logs --tail 60 app
    throw 'The application did not become ready at http://127.0.0.1:8080/api/health.'
}
Write-Host 'Caldova Pharmaceuticals is ready at http://127.0.0.1:8080'
