@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo Installing FastAI 0.1.3 module...
call mvn -q install -DskipTests
if errorlevel 1 (
    echo Failed to install FastAI module.
    pause
    exit /b 1
)

echo Compiling Demo project...
call mvn -f examples/Demo/pom.xml clean compile dependency:build-classpath "-Dmdep.outputFile=cp-cli.txt" -DskipTests -q
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

for /f "usebackq delims=" %%i in ("examples\Demo\cp-cli.txt") do set CP=%%i

:: Copy native DLLs to the current working directory so llama.cpp can load its CPU/JNI backends
copy /Y ..\FastAIModel\build\*.dll . >nul

:: Run with all native/vector flags needed for local inference
java --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector -cp "examples\Demo\target\classes;%CP%" DemoCLI %*

:: Clean up copied DLLs to keep the workspace tidy
del /Q *.dll

