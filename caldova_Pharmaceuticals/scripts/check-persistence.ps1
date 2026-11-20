. (Join-Path $PSScriptRoot 'common.ps1')
Initialize-DemoEnvironment
$curl = Get-Command curl -CommandType Application -ErrorAction Stop |
    Select-Object -First 1 -ExpandProperty Source
$cookie = [IO.Path]::GetTempFileName()

function Assert-HttpStatus([string]$Path, [int]$Expected) {
    $output = [IO.Path]::GetTempFileName()
    try {
        $status = & $curl --silent --show-error --max-time 15 -b $cookie -c $cookie -o $output -w '%{http_code}' "http://127.0.0.1:8080$Path"
        if ($LASTEXITCODE -ne 0 -or [int]$status -ne $Expected) {
            throw "Expected HTTP $Expected at $Path, received $status (curl exit $LASTEXITCODE)."
        }
        return [IO.File]::ReadAllText($output)
    } finally { Remove-Item -LiteralPath $output }
}

function Sign-InPresenter([int]$Expected) {
    $page = Assert-HttpStatus '/login' 200
    $match = [regex]::Match($page, 'name="_csrf"[^>]*value="([^"]+)"')
    if (-not $match.Success) { throw 'The sign-in form did not provide a CSRF token.' }
    $line = Get-Content -LiteralPath (Join-Path $ProjectRoot '.env') | Where-Object { $_ -match '^PRESENTER_PASSWORD=' }
    $password = $line.Substring('PRESENTER_PASSWORD='.Length)
    $data = 'username=presenter&password=' + [System.Net.WebUtility]::UrlEncode($password) +
        '&_csrf=' + [System.Net.WebUtility]::UrlEncode($match.Groups[1].Value)
    $output = [IO.Path]::GetTempFileName()
    try {
        $status = $data | & $curl --silent --show-error --max-time 15 -b $cookie -c $cookie `
            --data '@-' -o $output -w '%{http_code}' 'http://127.0.0.1:8080/login'
        if ($LASTEXITCODE -ne 0 -or [int]$status -ne $Expected) {
            throw "Expected sign-in HTTP $Expected, received $status."
        }
    } finally { Remove-Item -LiteralPath $output }
}

function Assert-OperationalRecords {
    $lines = Assert-HttpStatus '/lines' 200
    if ([regex]::Matches($lines, '/lines/\d+').Count -ne 160) { throw 'Expected 160 persisted lines.' }
    $maintenance = Assert-HttpStatus '/maintenance' 200
    if ([regex]::Matches($maintenance, 'href="/lines/\d+"').Count -ne 12) { throw 'Expected 12 maintenance records.' }
    $rota = Assert-HttpStatus '/rota' 200
    if ([regex]::Matches($rota, '\b(?:NB|WH|EM|SW)-EMP\d{3}\b').Count -ne 16) { throw 'Expected 16 employee assignments for the default day.' }
}

try {
    Sign-InPresenter 302
    Assert-OperationalRecords
    Invoke-Compose stop oracle
    try {
        $failure = Assert-HttpStatus '/lines' 503
        if ($failure -notmatch 'Database unavailable') { throw 'Expected an explicit database-unavailable page.' }
        $health = Assert-HttpStatus '/api/health' 503 | ConvertFrom-Json
        if ($health.status -ne 'DOWN') { throw 'Readiness must be DOWN while Oracle is stopped.' }
        [IO.File]::WriteAllText($cookie, '')
        Sign-InPresenter 503
    } finally {
        Invoke-Compose up -d --wait --wait-timeout 420 oracle
    }
    [IO.File]::WriteAllText($cookie, '')
    Sign-InPresenter 302
    Assert-OperationalRecords
    Assert-HttpStatus '/api/health' 200 | Out-Null
    Write-Host 'PASS: Oracle outage returns 503 for data and sign-in; restart preserves accounts, 160 lines, maintenance and employee rota.'
} finally { Remove-Item -LiteralPath $cookie }
