@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul 2>&1

set "INSTALL_DIR=%USERPROFILE%\ytdownloader"

echo ==================================================================
echo   ytdownloader - Windows Uninstaller
echo ==================================================================

:: 1. Laufende Instanzen beenden
taskkill /F /IM javaw.exe /FI "WINDOWTITLE eq YouTube Downloader*" >nul 2>&1

:: 2. Verknuepfungen loeschen
del /f /q "%APPDATA%\Microsoft\Windows\Start Menu\Programs\YouTube Downloader.lnk" >nul 2>&1
del /f /q "%USERPROFILE%\Desktop\YouTube Downloader.lnk" >nul 2>&1

:: 3. Programmordner loeschen
if exist "%INSTALL_DIR%" (
    echo [*] Loesche Programmordner: %INSTALL_DIR%
    rd /s /q "%INSTALL_DIR%"
)

echo ==================================================================
echo   [OK] ytdownloader wurde erfolgreich deinstalliert!
echo ==================================================================
pause
