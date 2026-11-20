param([switch]$ConfirmReset)
. (Join-Path $PSScriptRoot 'common.ps1')
if (-not $ConfirmReset) { throw 'Reset removes only fictional Caldova records. Re-run with -ConfirmReset to approve.' }
Initialize-DemoEnvironment
Invoke-Compose exec -T oracle sqlplus -s / as sysdba '@/opt/caldova/sql/reset-demo.sql'
