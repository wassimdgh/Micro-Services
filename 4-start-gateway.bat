@echo off
echo Starting Gateway Service...
cd gateway-service
start cmd /k "mvn spring-boot:run"
cd ..
echo Gateway Service started on port 8080
