@echo off
cd /d "%~dp0"
if "%DB_PASSWORD%"=="" set /p DB_PASSWORD=MySQL root password: 
del /q *.class 2>nul
javac *.java
if errorlevel 1 (
    pause
    exit /b
)
java Main
pause
