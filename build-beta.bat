@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title Pick Climber - Beta build

set "GRADLE_VERSION=9.2.1"
set "DIST_ROOT=%CD%\.gradle-dist"
set "DIST_DIR=%DIST_ROOT%\gradle-%GRADLE_VERSION%"
set "DIST_ZIP=%DIST_ROOT%\gradle-%GRADLE_VERSION%-bin.zip"
set "JAVA_EXE="
set "BUILD_FAILED=0"

 echo ============================================================
 echo        PICK CLIMBER - BETA BUILD 0.1.27
 echo ============================================================
 echo Directory: %CD%
 echo.

rem ------------------------------------------------------------
rem 1. Find Java through PATH, JAVA_HOME, or Prism-managed runtimes.
rem ------------------------------------------------------------
where java.exe >nul 2>nul
if not errorlevel 1 (
    for /f "delims=" %%J in ('where java.exe') do if not defined JAVA_EXE set "JAVA_EXE=%%J"
)

if not defined JAVA_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

if not defined JAVA_EXE (
    echo Java is not in PATH. Searching for a Prism Launcher installation...
    for /f "usebackq delims=" %%J in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$roots=@($env:APPDATA+'\PrismLauncher\java',$env:LOCALAPPDATA+'\PrismLauncher\java',$env:LOCALAPPDATA+'\Programs\PrismLauncher\java',$env:ProgramFiles+'\PrismLauncher\java',$env:ProgramFiles+'\Eclipse Adoptium',$env:ProgramFiles+'\Java'); foreach($root in $roots){if(Test-Path -LiteralPath $root){$found=Get-ChildItem -LiteralPath $root -Filter java.exe -File -Recurse -ErrorAction SilentlyContinue ^| Where-Object {$_.FullName -match '\\bin\\java\.exe$'} ^| Select-Object -First 1; if($found){$found.FullName; break}}}"`) do if not defined JAVA_EXE set "JAVA_EXE=%%J"
)

if not defined JAVA_EXE goto :java_missing
if not exist "%JAVA_EXE%" goto :java_missing

for %%I in ("%JAVA_EXE%") do set "JAVA_BIN=%%~dpI"
for %%I in ("%JAVA_BIN%..") do set "JAVA_HOME=%%~fI"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Java found:
echo   %JAVA_EXE%
"%JAVA_EXE%" -version
echo.
if errorlevel 1 goto :java_broken

rem ------------------------------------------------------------
rem 2. Download Gradle if it is not available locally yet.
rem ------------------------------------------------------------
if not exist "%DIST_DIR%\bin\gradle.bat" (
    echo Gradle %GRADLE_VERSION% has not been downloaded.
    if not exist "%DIST_ROOT%" mkdir "%DIST_ROOT%"
    if errorlevel 1 goto :mkdir_failed

    if exist "%DIST_ZIP%" del /q "%DIST_ZIP%"

    echo Downloading Gradle %GRADLE_VERSION%...
    where curl.exe >nul 2>nul
    if not errorlevel 1 (
        curl.exe -L --fail --retry 3 --output "%DIST_ZIP%" "https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"
    ) else (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%DIST_ZIP%'"
    )
    if errorlevel 1 goto :download_failed
    if not exist "%DIST_ZIP%" goto :download_failed

    echo.
    echo Extracting Gradle...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%DIST_ZIP%' -DestinationPath '%DIST_ROOT%' -Force"
    if errorlevel 1 goto :extract_failed
)

if not exist "%DIST_DIR%\bin\gradle.bat" goto :gradle_missing

rem ------------------------------------------------------------
rem 3. Build. --stacktrace leaves useful diagnostics on failure.
rem ------------------------------------------------------------
echo.
echo Building Pick Climber...
echo The first build may download NeoForge dependencies.
echo.
call "%DIST_DIR%\bin\gradle.bat" --no-daemon clean build --stacktrace
if errorlevel 1 goto :build_failed

set "JAR_FILE="
for /f "delims=" %%F in ('dir /b /a-d "build\libs\*.jar" 2^>nul') do if not defined JAR_FILE set "JAR_FILE=build\libs\%%F"

if not defined JAR_FILE goto :jar_missing

echo.
echo ============================================================
echo BUILD COMPLETED SUCCESSFULLY
echo Generated JAR:
echo   %CD%\%JAR_FILE%
echo ============================================================
goto :success

:java_missing
echo.
echo ERROR: Java was not found to run Gradle.
echo.
echo Prism can run Minecraft with an internal Java runtime without adding it to PATH.
echo Solutions:
echo   1. In Prism: Settings ^> Java ^> open or copy the Java path.
echo   2. Install Java 21, for example Eclipse Temurin 21.
echo   3. Open CMD here and run:
echo        set "JAVA_HOME=C:\path\to\java-21"
echo        build-beta.bat
goto :failure

:java_broken
echo ERROR: The Java installation found cannot be executed.
goto :failure

:mkdir_failed
echo ERROR: Could not create the "%DIST_ROOT%" directory.
goto :failure

:download_failed
echo.
echo ERROR: Gradle could not be downloaded.
echo Check the internet connection, antivirus, proxy, or firewall.
echo URL:
echo https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip
goto :failure

:extract_failed
echo ERROR: Gradle was downloaded but could not be extracted.
echo Delete the .gradle-dist directory and run this file again.
goto :failure

:gradle_missing
echo ERROR: "%DIST_DIR%\bin\gradle.bat" does not exist after extraction.
goto :failure

:build_failed
echo.
echo ============================================================
echo BUILD FAILED
 echo Copy everything from "FAILURE: Build failed" to the end and send it.
echo ============================================================
goto :failure

:jar_missing
echo ERROR: Gradle finished, but no JAR was found in build\libs\.
goto :failure

:failure
set "BUILD_FAILED=1"
echo.
echo This window will remain open so the error can be read or copied.
pause
endlocal & exit /b 1

:success
echo.
echo You can copy the JAR to the Prism Launcher instance's mods directory.
pause
endlocal & exit /b 0
