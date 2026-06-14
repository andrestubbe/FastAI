@echo off
chcp 65001 >nul
cd /d "%~dp0"
cd examples\Demo
call mvn compile "dependency:build-classpath" "-Dmdep.outputFile=cp.txt" "-DincludeScope=runtime" -q
if %ERRORLEVEL% NEQ 0 ( echo Compile failed. & pause & exit /b %ERRORLEVEL% )

rem Build a Java @argfile inlined — avoids the 1023-char set /p truncation limit
powershell -ExecutionPolicy Bypass -Command "$cp = (Get-Content 'cp.txt' -Raw).Trim().Replace('\', '/'); $base = (Resolve-Path 'target\classes').Path.Replace('\', '/'); $argLine = '-cp \"' + $base + ';' + $cp + '\"'; Set-Content 'cp-arg.txt' $argLine -Encoding ASCII"
if %ERRORLEVEL% NEQ 0 ( echo Failed to build argfile. & pause & exit /b %ERRORLEVEL% )

java --enable-native-access=ALL-UNNAMED @cp-arg.txt Demo2

cd ..\..
Pause
