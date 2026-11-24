$ErrorActionPreference = "Stop"

$clave = Read-Host "Password de MySQL" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $clave).Password

$secret = [Environment]::GetEnvironmentVariable("JWT_SECRET", "User")

if ([string]::IsNullOrWhiteSpace($secret)) {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $secret = [Convert]::ToBase64String($bytes)
    $rng.Dispose()
}

$env:JWT_SECRET = $secret

[Environment]::SetEnvironmentVariable("JWT_SECRET", $env:JWT_SECRET, "User")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", $env:DB_PASSWORD, "User")

Write-Host "Variables configuradas correctamente." -ForegroundColor Green
