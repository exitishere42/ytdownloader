@echo off
setlocal
cd /d "%~dp0"

if exist "%~dp0target\ytdownloader-1.0.0-all.jar" (
    start "" javaw -jar "%~dp0target\ytdownloader-1.0.0-all.jar" %*
) else if exist "%~dp0ytdownloader.jar" (
    start "" javaw -jar "%~dp0ytdownloader.jar" %*
) else (
    call mvn javafx:run
)
