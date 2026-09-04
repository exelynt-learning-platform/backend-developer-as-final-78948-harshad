@echo off
setlocal

set MAVEN_DIR=%~dp0.mvn\apache-maven-3.9.6
if exist "%MAVEN_DIR%\bin\mvn.cmd" (
    "%MAVEN_DIR%\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

echo Maven distribution not found locally. Downloading Apache Maven...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$zipPath = Join-Path $env:TEMP 'apache-maven-3.9.6-bin.zip'; $destDir = Join-Path '%~dp0' '.mvn'; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip' -OutFile $zipPath; Expand-Archive -Path $zipPath -DestinationPath $destDir -Force; Remove-Item -Path $zipPath -Force"

if exist "%MAVEN_DIR%\bin\mvn.cmd" (
    "%MAVEN_DIR%\bin\mvn.cmd" %*
) else (
    echo Failed to initialize Maven.
    exit /b 1
)
