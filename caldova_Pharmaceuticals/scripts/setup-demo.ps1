. (Join-Path $PSScriptRoot 'common.ps1')
Initialize-DemoEnvironment
Invoke-Compose up -d --wait --wait-timeout 420 oracle
Invoke-Compose exec -T oracle sh -c 'sqlplus -s / as sysdba @/opt/caldova/sql/setup.sql "$CALDOVA_OWNER_PASSWORD" "$CALDOVA_READ_PASSWORD"'
Invoke-Compose run --rm --no-deps verify mvn -B -ntp compile exec:exec '-Dexec.executable=java' '-Dexec.args=-classpath %classpath com.caldova.production.setup.PresenterAccountSetup'
Write-Host 'Oracle demo setup completed. Run start-demo.ps1 to open the application.'
