$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path -Parent $PSScriptRoot

function Invoke-Compose {
    & docker compose --project-directory $ProjectRoot -f (Join-Path $ProjectRoot 'compose.yaml') @args
    if ($LASTEXITCODE -ne 0) { throw "Caldova Docker Compose command failed (exit $LASTEXITCODE)." }
}

function New-DemoPassword {
    $bytes = New-Object byte[] 24
    $random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $random.GetBytes($bytes) } finally { $random.Dispose() }
    return 'C' + ([BitConverter]::ToString($bytes) -replace '-', '')
}

function Initialize-DemoEnvironment {
    $path = Join-Path $ProjectRoot '.env'
    if (-not (Test-Path -LiteralPath $path)) {
        & docker volume inspect caldova-oracle-data *> $null
        if ($LASTEXITCODE -eq 0) {
            throw 'The demo Oracle volume already exists, but .env is missing. Restore the original .env. Do not delete the volume.'
        }
        $values = @(
            "ORACLE_PWD=$(New-DemoPassword)"
            "CALDOVA_OWNER_PASSWORD=$(New-DemoPassword)"
            "CALDOVA_READ_PASSWORD=$(New-DemoPassword)"
        )
        [IO.File]::WriteAllLines($path, $values, (New-Object Text.UTF8Encoding($false)))
        Write-Host 'Created ignored local credentials in .env. Keep this file with the demo volume.'
    }
    foreach ($key in @('ORACLE_PWD', 'CALDOVA_OWNER_PASSWORD', 'CALDOVA_READ_PASSWORD')) {
        $line = @(Get-Content -LiteralPath $path | Where-Object { $_ -match "^$key=" })
        if ($line.Count -ne 1 -or $line[0] -notmatch "^$key=[A-Za-z][A-Za-z0-9]{23,}$") {
            throw "Set $key in .env to a unique alphanumeric password starting with a letter (at least 24 characters)."
        }
    }
    if (-not (Select-String -LiteralPath $path -Pattern '^PRESENTER_PASSWORD=' -Quiet)) {
        Add-Content -LiteralPath $path -Value "PRESENTER_PASSWORD=$(New-DemoPassword)"
        Write-Host 'Added presenter credentials to ignored .env. Username: presenter.'
    }
    $presenter = @(Get-Content -LiteralPath $path | Where-Object { $_ -match '^PRESENTER_PASSWORD=' })
    if ($presenter.Count -ne 1 -or $presenter[0] -notmatch '^PRESENTER_PASSWORD=[A-Za-z0-9!_@%+.-]{12,64}$') {
        throw 'Set a unique presenter password in .env (12-64 characters using letters, digits, or !_@%+.-).'
    }
}
