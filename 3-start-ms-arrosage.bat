@echo off
echo ========================================
echo Starting ms-arrosage (Irrigation Service)
echo Port: 8082
echo Profile: default (Config Server, CloudAMQP, MySQL)
echo ========================================
cd /d "%~dp0ms-arrosage"
start "MS-ARROSAGE" cmd /k "mvn spring-boot:run || echo Maven not found! Please install Maven or use your IDE."
echo.
echo ms-arrosage is starting...
echo API will be available at: http://localhost:8082/api/arrosage
echo Swagger UI: http://localhost:8082/swagger-ui.html
echo.
pause
