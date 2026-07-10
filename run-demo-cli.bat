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

:: Run with all native/vector flags needed for local inference
:: DLLs are loaded directly from FastAIModel\lib - no copying needed
for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command "(Resolve-Path '..\FastAIModel\lib').Path"`) do set NATIVE_LIB=%%i
java --enable-native-access=ALL-UNNAMED -Djava.library.path="%NATIVE_LIB%" -cp "examples\Demo\target\classes;%CP%" DemoCLI %*

