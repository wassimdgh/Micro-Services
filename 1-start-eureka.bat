@echo off
echo ========================================
echo Starting Eureka Server on port 8070
echo ========================================
cd /d "%~dp0eureka-server"
start "Eureka Server" cmd /k "mvn spring-boot:run || echo Maven not found! Please install Maven or use your IDE."
echo.
echo Eureka Server is starting...
echo Access it at: http://localhost:8070
echo.
pause
