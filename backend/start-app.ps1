param([switch]$NoBuild)

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -LiteralPath $projectDir

Write-Host "=== Timetable Scheduler Startup ===" -ForegroundColor Cyan

# Copy resources (IntelliJ rebuilds wipe target/classes)
Write-Host "[1/2] Copying resources to target/classes..." -ForegroundColor Cyan
Copy-Item -Path "$projectDir\src\main\resources\*" -Destination "$projectDir\target\classes\" -Recurse -Force

Write-Host "[2/2] Starting Timetable Scheduler..." -ForegroundColor Cyan
Write-Host "API:        http://localhost:8080" -ForegroundColor Green
Write-Host "Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host "`nPress Ctrl+C to stop.`n" -ForegroundColor Yellow

# Run with classpath from IntelliJ's Maven repo
$m2 = "$env:USERPROFILE\.m2\repository"
$cp = "$projectDir\target\classes"

$jars = @(
    "$m2\org\springframework\boot\spring-boot-starter-actuator\3.2.5\spring-boot-starter-actuator-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot-starter\3.2.5\spring-boot-starter-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot\3.2.5\spring-boot-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot-autoconfigure\3.2.5\spring-boot-autoconfigure-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot-starter-logging\3.2.5\spring-boot-starter-logging-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot-starter-web\3.2.5\spring-boot-starter-web-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot-starter-data-jpa\3.2.5\spring-boot-starter-data-jpa-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot-starter-security\3.2.5\spring-boot-starter-security-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot-starter-tomcat\3.2.5\spring-boot-starter-tomcat-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot-starter-json\3.2.5\spring-boot-starter-json-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot-starter-aop\3.2.5\spring-boot-starter-aop-3.2.5.jar"
    "$m2\org\springframework\boot\spring-boot-starter-jdbc\3.2.5\spring-boot-starter-jdbc-3.2.5.jar"
    "$m2\org\springframework\spring-webmvc\6.1.6\spring-webmvc-6.1.6.jar"
    "$m2\org\springframework\spring-web\6.1.6\spring-web-6.1.6.jar"
    "$m2\org\springframework\spring-context\6.1.6\spring-context-6.1.6.jar"
    "$m2\org\springframework\spring-beans\6.1.6\spring-beans-6.1.6.jar"
    "$m2\org\springframework\spring-core\6.1.6\spring-core-6.1.6.jar"
    "$m2\org\springframework\spring-jcl\6.1.6\spring-jcl-6.1.6.jar"
    "$m2\org\springframework\spring-expression\6.1.6\spring-expression-6.1.6.jar"
    "$m2\org\springframework\spring-aop\6.1.6\spring-aop-6.1.6.jar"
    "$m2\org\springframework\spring-orm\6.1.6\spring-orm-6.1.6.jar"
    "$m2\org\springframework\spring-tx\6.1.6\spring-tx-6.1.6.jar"
    "$m2\org\springframework\spring-jdbc\6.1.6\spring-jdbc-6.1.6.jar"
    "$m2\org\springframework\data\spring-data-jpa\3.2.5\spring-data-jpa-3.2.5.jar"
    "$m2\org\springframework\data\spring-data-commons\3.2.5\spring-data-commons-3.2.5.jar"
    "$m2\org\hibernate\orm\hibernate-core\6.4.4.Final\hibernate-core-6.4.4.Final.jar"
    "$m2\org\springframework\security\spring-security-web\6.2.4\spring-security-web-6.2.4.jar"
    "$m2\org\springframework\security\spring-security-config\6.2.4\spring-security-config-6.2.4.jar"
    "$m2\org\springframework\security\spring-security-core\6.2.4\spring-security-core-6.2.4.jar"
    "$m2\com\github\librepdf\openpdf\1.3.39\openpdf-1.3.39.jar"
    "$m2\org\apache\pdfbox\pdfbox\3.0.2\pdfbox-3.0.2.jar"
    "$m2\org\apache\poi\poi-ooxml\5.2.5\poi-ooxml-5.2.5.jar"
    "$m2\org\apache\poi\poi\5.2.5\poi-5.2.5.jar"
    "$m2\org\xhtmlrenderer\flying-saucer-core\9.3.1\flying-saucer-core-9.3.1.jar"
    "$m2\org\projectlombok\lombok\1.18.32\lombok-1.18.32.jar"
    "$m2\org\springdoc\springdoc-openapi-starter-webmvc-ui\2.5.0\springdoc-openapi-starter-webmvc-ui-2.5.0.jar"
    "$m2\io\jsonwebtoken\jjwt-api\0.11.5\jjwt-api-0.11.5.jar"
    "$m2\io\jsonwebtoken\jjwt-impl\0.11.5\jjwt-impl-0.11.5.jar"
    "$m2\io\jsonwebtoken\jjwt-jackson\0.11.5\jjwt-jackson-0.11.5.jar"
    "$m2\com\fasterxml\jackson\core\jackson-databind\2.15.4\jackson-databind-2.15.4.jar"
    "$m2\com\fasterxml\jackson\core\jackson-annotations\2.15.4\jackson-annotations-2.15.4.jar"
    "$m2\com\fasterxml\jackson\core\jackson-core\2.15.4\jackson-core-2.15.4.jar"
    "$m2\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar"
    "$m2\ch\qos\logback\logback-classic\1.4.14\logback-classic-1.4.14.jar"
    "$m2\org\slf4j\slf4j-api\2.0.13\slf4j-api-2.0.13.jar"
)
$classpath = ($jars -join ";") + ";" + $cp

$jdk = "C:\Users\mahmo\.jdks\ms-21.0.9\bin\java.exe"
& $jdk -cp "$classpath" com.example.timetable.TimetableSchedulerApplication
