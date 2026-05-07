@REM Maven Wrapper startup batch script
@REM -------------------------------------------------------------------
@echo off
setlocal

set JAVA_HOME=C:\Program Files\Java\jdk-17
set MAVEN_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3
set PATH=%MAVEN_HOME%\bin;%PATH%

call "%MAVEN_HOME%\bin\mvn.cmd" %*

endlocal
