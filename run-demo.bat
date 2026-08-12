@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo [FastAI] Building core library...
call mvn install -DskipTests -q
if errorlevel 1 (
    echo [ERROR] Core build failed.
    exit /b 1
)

if not exist "examples\Demo\target\classes" (
    echo [FastAI] Compiling Demo...
    call mvn -f examples\Demo\pom.xml compile dependency:build-classpath "-Dmdep.outputFile=cp.txt" -DskipTests -q
) else (
    call mvn -f examples\Demo\pom.xml compile -DskipTests -q
)

if not exist "examples\Demo\cp.txt" (
    call mvn -f examples\Demo\pom.xml dependency:build-classpath "-Dmdep.outputFile=cp.txt" -DskipTests -q
)

set /p CP=<"examples\Demo\cp.txt"
java -cp "examples\Demo\target\classes;%CP%" Demo %*


