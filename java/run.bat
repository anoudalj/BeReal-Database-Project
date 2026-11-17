@echo off
cd /d "%~dp0"

"C:\Program Files\JetBrains\PyCharm 2026.2.2\jbr\bin\javac" --release 17 -cp ".;lib\mysql-connector-j-26.10.0.jar" AdvancedQueriesGUI.java DBConnection.java
if errorlevel 1 (
    pause
    exit /b
)

"C:\Program Files\JetBrains\PyCharm 2026.2.2\jbr\bin\java" -cp ".;lib\mysql-connector-j-26.10.0.jar" AdvancedQueriesGUI
pause