@echo off
echo ========================================
echo Starting ms-meteo (Weather Service)
echo Port: 8081
echo Profile: default (Config Server, CloudAMQP, MySQL)
echo ========================================
cd /d "%~dp0ms-meteo"
start "MS-METEO" cmd /k "mvn spring-boot:run || echo Maven not found! Please install Maven or use your IDE."
echo.
echo ms-meteo is starting...
echo API will be available at: http://localhost:8081/api/meteo
echo Swagger UI: http://localhost:8081/swagger-ui.html
echo.
pause
