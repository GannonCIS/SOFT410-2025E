@echo off
:: -----------------------------------------------------------------------------
:: Gradle start up script for Windows
:: -----------------------------------------------------------------------------

setlocal
set DIRNAME=%~dp0

rem Add default JVM options here. You can also use JAVA_OPS and GRADLE_OPTS to pass JVM options to this script.
if not defined JAVA_OPTS set JAVA_OPTS=-Xmx64m

rem Resolve JAVA executable
if defined JAVA_HOME (
	set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
	set JAVA_EXE=java
)

rem Run the wrapper using the wrapper jar on the classpath and the wrapper main class
"%JAVA_EXE%" %JAVA_OPTS% -cp "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

endlocal
