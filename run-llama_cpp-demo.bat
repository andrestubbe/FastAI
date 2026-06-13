@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ⚡ [FastAI] Starte Llama.cpp Demo (leise)...
cd examples\00-basic-usage
call mvn -q compile exec:java -Dexec.mainClass=fastai.examples.LlamaCppDemo
cd ..\..
pause
