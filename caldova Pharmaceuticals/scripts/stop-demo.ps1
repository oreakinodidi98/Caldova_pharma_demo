. (Join-Path $PSScriptRoot 'common.ps1')
Invoke-Compose stop app oracle
Write-Host 'Caldova containers are stopped. The database volume and local credentials were preserved.'
