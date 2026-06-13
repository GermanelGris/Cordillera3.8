# =============================================================================
# build.ps1 — Compila todos los microservicios con JDK 17 (Grupo Cordillera)
#
# El proyecto está fijado en Java 17 (<java.version>17</java.version> y
# Dockerfiles eclipse-temurin:17). El `java` por defecto del sistema es JDK 23,
# con el que Lombok NO genera código y la compilación falla. Este script fuerza
# el uso de JDK 17 para el build, sin alterar tu configuración global.
#
# Uso:
#   .\build.ps1            # compila todos los servicios (sin tests)
#   .\build.ps1 -Package   # empaqueta los .jar (sin tests)
#   .\build.ps1 -Test      # compila y ejecuta los tests
# =============================================================================
param(
    [switch]$Package,
    [switch]$Test
)

# Ruta del JDK 17 (ajusta si tu instalación está en otra ubicación)
$jdk17 = "C:\Program Files\Java\jdk-17"
if (-not (Test-Path $jdk17)) {
    Write-Host "ERROR: No se encontró JDK 17 en '$jdk17'. Instálalo o edita la ruta en build.ps1." -ForegroundColor Red
    exit 1
}
$env:JAVA_HOME = $jdk17
Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Cyan

# Fase de Maven a ejecutar
if ($Package)   { $goal = "package" }
elseif ($Test)  { $goal = "verify"  }
else            { $goal = "compile" }

$mvnArgs = @($goal)
if (-not $Test) { $mvnArgs += "-DskipTests" }

$servicios = @("MS-login", "MS-data", "MS-kpi", "MS-reportes", "api-gateway")
$root = $PSScriptRoot

foreach ($svc in $servicios) {
    Write-Host "`n=== $svc :: mvn $($mvnArgs -join ' ') ===" -ForegroundColor Yellow
    & "$root\$svc\mvnw.cmd" -f "$root\$svc\pom.xml" -B @mvnArgs
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Falló la compilación de $svc" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`nTodos los servicios compilaron con Java 17 (JDK 17.x)" -ForegroundColor Green
