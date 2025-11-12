@echo off
REM ========================================
REM   Employer-Worker Registration System
REM   Quick Test - Verify Database Works
REM ========================================

echo.
echo Testing database connection and sample data...
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk-23"

call gradlew.bat -q compileJava

echo.
echo Running database test...
echo.

java -cp "build/classes/java/main;%USERPROFILE%\.gradle\caches\modules-2\files-2.1\com.h2database\h2\2.2.224\*\*.jar" com.cbozan.dao.DBTest

echo.
echo ========================================
echo Test Complete!
echo ========================================
pause
