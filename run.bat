@echo off
setlocal
cd /d "%~dp0"

echo ===================================================
echo Starting PLATRA Railway Reservation System...
echo ===================================================

set JAVA_EXE=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Users\sumit\.jdks\openjdk-26.0.1\bin\java.exe" (
        set "JAVA_EXE=C:\Users\sumit\.jdks\openjdk-26.0.1\bin\java.exe"
    ) else if defined JAVA_HOME (
        set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    ) else (
        echo [ERROR] Java could not be found. Please ensure JDK/JRE is installed.
        pause
        exit /b 1
    )
)

"%JAVA_EXE%" -cp "out\production\College Project;.;lib\mysql-connector-j-8.3.0.jar" RailwayReservationSystem
pause
