@echo off
title FindIT - Smart Campus Lost & Found System
echo ========================================================
echo   FindIT - Smart Campus Lost & Found Web Application
echo   Java Backend + REST API + Modern Web Frontend
echo ========================================================
echo.
echo Starting Java REST API Server on port 8080...
echo Opening Web App at http://localhost:8080
echo.
start "" http://localhost:8080
java -jar "%~dp0target\findit-1.0.0.jar"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error.
    pause
)
