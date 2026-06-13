@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo 🚀 [FastAI] Running Demo (leise)...
cd examples\Demo
call mvn -q compile exec:java -Dexec.mainClass=fastai.examples.BasicAIDemo
cd ..\..
