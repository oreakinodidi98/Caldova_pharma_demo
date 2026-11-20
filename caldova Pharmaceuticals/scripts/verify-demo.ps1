. (Join-Path $PSScriptRoot 'common.ps1')
& (Join-Path $PSScriptRoot 'setup-demo.ps1')
Invoke-Compose run --rm --no-deps verify
