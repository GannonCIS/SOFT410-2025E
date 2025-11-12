@echo off
rem Robust gradle-run helper
rem - Avoid hardcoding JAVA_HOME unless the path exists
rem - Locate the project's gradlew.bat automatically and call it

setlocal
set "SCRIPT_DIR=%~dp0"

rem If JAVA_HOME is not set but a common JDK path exists, use it. Otherwise do not override user config.
if not defined JAVA_HOME (
	if exist "C:\Program Files\Java\jdk-23\bin\java.exe" (
		set "JAVA_HOME=C:\Program Files\Java\jdk-23"
		set "PATH=%JAVA_HOME%\bin;%PATH%"
	)
)

echo Checking Java installation...
java -version 2>nul || echo Java not found on PATH or JAVA_HOME. Please install a JDK or set JAVA_HOME.
echo.

rem Find gradlew.bat: prefer one in the same folder as this helper, then check common subfolder, then search recursively
set "WRAPPER="
if exist "%SCRIPT_DIR%gradlew.bat" (
	set "WRAPPER=%SCRIPT_DIR%gradlew.bat"
) else if exist "%SCRIPT_DIR%employer-worker-registration-system-main\gradlew.bat" (
	set "WRAPPER=%SCRIPT_DIR%employer-worker-registration-system-main\gradlew.bat"
) else (
	for /r "%SCRIPT_DIR%" %%F in (gradlew.bat) do (
		set "WRAPPER=%%~fF"
		goto :FOUND_WRAPPER
	)
)

:FOUND_WRAPPER
if not defined WRAPPER (
	echo ERROR: gradlew.bat not found. Ensure the Gradle wrapper exists in the project.
	endlocal
	exit /b 1
)

echo Using wrapper: %WRAPPER%
call "%WRAPPER%" %*

endlocal
