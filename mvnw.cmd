@echo off
setlocal
set "BASE=%~dp0"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.11"
set "MAVEN_DIR=%MAVEN_HOME%\apache-maven-3.9.11"
if not exist "%MAVEN_DIR%\bin\mvn.cmd" (
  echo Maven Wrapper: downloading Maven 3.9.11...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop'; New-Item -ItemType Directory -Force -Path '%MAVEN_HOME%' | Out-Null; ^
     Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip' -OutFile '%MAVEN_HOME%\maven.zip'; ^
     Expand-Archive -Force '%MAVEN_HOME%\maven.zip' '%MAVEN_HOME%'; Remove-Item '%MAVEN_HOME%\maven.zip'"
  if errorlevel 1 exit /b 1
)
call "%MAVEN_DIR%\bin\mvn.cmd" %*
endlocal
