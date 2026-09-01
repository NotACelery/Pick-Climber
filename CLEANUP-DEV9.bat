@echo off
setlocal
set "ROOT=%~dp0"
set "OLD_ENTRY=%ROOT%src\main\java\dev\maicra\pickclimber\client\PickClimberOptionsEntry.java"
if exist "%OLD_ENTRY%" del /q "%OLD_ENTRY%"
if exist "%ROOT%MIGRATE-DOCS-DEV8.bat" del /q "%ROOT%MIGRATE-DOCS-DEV8.bat"
echo Pick Climber dev.9 cleanup complete.
endlocal
