@echo off
REM Run Employer-Worker Registration System with H2 Database
REM No installation required - works immediately!

echo ========================================
echo Employer-Worker Registration System
echo Using H2 Embedded Database
echo ========================================
echo.

REM Set JAVA_HOME if JDK 23 is installed
if exist "C:\Program Files\Java\jdk-23" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-23"
    echo Using Java 23: %JAVA_HOME%
) else (
    echo Using system Java
)

echo.
echo Building and running application...
echo.

REM Run the application
call gradlew.bat run --no-daemon

echo.
echo Application closed.
pause
