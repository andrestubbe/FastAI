@echo off
setlocal
echo =======================================================
echo   FastAI JMH Microbenchmark Runner
echo =======================================================
java -jar target\benchmarks.jar -f 1 -wi 2 -i 3 -r 2 -w 2
pause
