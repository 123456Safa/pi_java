@REM Maven Wrapper startup batch script
@REM -------------------------------------------------------------------
@echo off
setlocal

set JAVA_HOME=C:\Users\fatma\.jdks\openjdk-26
set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"

"%JAVA_HOME%\bin\java.exe" -jar %WRAPPER_JAR% %*

endlocal
