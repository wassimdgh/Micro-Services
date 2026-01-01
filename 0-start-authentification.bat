@echo off
echo ========================================
echo Starting Authentification Service on port 8085
echo ========================================
cd /d "%~dp0authentification"
start "Authentification Service" cmd /k "mvn spring-boot:run || echo Maven not found! Please install Maven or use your IDE."
echo.
echo Authentification Service is starting...
echo Access Swagger UI at: http://localhost:8085/swagger-ui.html
echo.
pause
