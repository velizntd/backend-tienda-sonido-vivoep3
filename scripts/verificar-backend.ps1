$servicios = [ordered]@{
    "eureka-server" = 8761
    "api-gateway"   = 8080
    "ms-productos"  = 8081
    "ms-novedades"  = 8082
    "ms-contacto"   = 8083
    "ms-carrito"    = 8084
    "ms-usuarios"   = 8085
    "ms-registro"   = 8086
    "ms-blogs"      = 8087
}

$resultados = foreach ($nombre in $servicios.Keys) {

    $puerto = $servicios[$nombre]

    $activo = [bool](Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue)

    $swagger = "N/A"

    if ($nombre -like "ms-*") {
        $swagger = curl.exe -s --max-time 5 -o NUL -w "%{http_code}" "http://localhost:$puerto/v3/api-docs"
    }

    [PSCustomObject]@{
        Servicio = $nombre
        Puerto = $puerto
        Activo = $activo
        Swagger = $swagger
    }
}

$resultados | Format-Table -AutoSize
