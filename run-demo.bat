@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo Installing local FastAI module...
call mvn -q install -DskipTests
if errorlevel 1 (
    echo Failed to install FastAI module.
    pause
    exit /b 1
)
cd examples\Demo
call mvn -q clean compile
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)
call mvn -q exec:java "-Dexec.mainClass=Demo" 2>&1
pause
cd ..\..
