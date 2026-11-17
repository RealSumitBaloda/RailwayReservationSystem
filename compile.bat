@echo off
setlocal
cd /d "%~dp0"

echo ===================================================
echo Compiling PLATRA Railway Reservation System...
echo ===================================================

set JAVAC_EXE=javac
where javac >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Users\sumit\.jdks\openjdk-26.0.1\bin\javac.exe" (
        set "JAVAC_EXE=C:\Users\sumit\.jdks\openjdk-26.0.1\bin\javac.exe"
    ) else if defined JAVA_HOME (
        set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"
    ) else (
        echo [ERROR] Javac compiler could not be found. Please install JDK.
        pause
        exit /b 1
    )
)

if not exist "out\production\College Project" mkdir "out\production\College Project"

"%JAVAC_EXE%" -d "out\production\College Project" -cp ".;lib\mysql-connector-j-8.3.0.jar" *.java
"%JAVAC_EXE%" -cp ".;lib\mysql-connector-j-8.3.0.jar" *.java

if %errorlevel% equ 0 (
    echo [SUCCESS] Compilation finished successfully.
) else (
    echo [ERROR] Compilation failed.
)
pause
