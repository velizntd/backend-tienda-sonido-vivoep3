$ErrorActionPreference = "Stop"

$raiz = Split-Path -Parent $PSScriptRoot

$env:JWT_SECRET = [Environment]::GetEnvironmentVariable("JWT_SECRET", "User")
$env:DB_PASSWORD = [Environment]::GetEnvironmentVariable("DB_PASSWORD", "User")

if (!$env:JWT_SECRET -or !$env:DB_PASSWORD) {
    throw "Primero ejecuta configurar-entorno.ps1"
}

$servicios = [ordered]@{
    "eureka-server" = 8761
    "api-gateway"   = 8080
    "ms-usuarios"   = 8085
    "ms-registro"   = 8086
    "ms-productos"  = 8081
    "ms-carrito"    = 8084
    "ms-contacto"   = 8083
    "ms-novedades"  = 8082
    "ms-blogs"      = 8087
}

foreach ($servicio in $servicios.Keys) {

    $puerto = $servicios[$servicio]

    if (Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue) {
        Write-Host "$servicio : Puerto ocupado, se omite." -ForegroundColor Yellow
        continue
    }

    Start-Process powershell.exe `
        -WorkingDirectory (Join-Path $raiz $servicio) `
        -ArgumentList "-NoExit", "-Command", ".\mvnw.cmd spring-boot:run"

    $limite = (Get-Date).AddMinutes(2)

    do {
        Start-Sleep -Seconds 2
        $activo = Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue
    } until ($activo -or (Get-Date) -ge $limite)

    if (!$activo) {
        throw "$servicio no inicio. Revisa su terminal."
    }

    Write-Host "$servicio : Puerto $puerto activo" -ForegroundColor Green
}
