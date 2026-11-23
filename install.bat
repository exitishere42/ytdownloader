@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul 2>&1

set "REPO=exitishere42/ytdownloader"
set "INSTALL_DIR=%USERPROFILE%\ytdownloader"
set "JAR_NAME=ytdownloader-1.2.0-all.jar"
set "RELEASE_URL=https://github.com/%REPO%/releases/latest/download"
set "RAW_URL=https://raw.githubusercontent.com/%REPO%/main"

echo ==================================================================
echo   ytdownloader - Windows Installer
echo   Zielordner: %INSTALL_DIR%
echo ==================================================================

:: ------------------------------------------------------------------------------
:: 1. Pruefen ob Java 21+ installiert ist
:: ------------------------------------------------------------------------------
set "JAVA_OK=0"
where java >nul 2>&1
if errorlevel 1 goto :check_java_done
powershell -NoProfile -Command "$v = (java -version 2>&1 | Out-String); if ($v -match '\"(\d+)' -and [int]$Matches[1] -ge 21) { exit 0 } else { exit 1 }" >nul 2>&1
if not errorlevel 1 set "JAVA_OK=1"
:check_java_done

if "%JAVA_OK%"=="1" (
    echo [OK] Java 21+ ist bereits installiert - ueberspringe Java-Installation.
) else (
    echo [*] Java 21+ nicht gefunden. Installiere OpenJDK 21 LTS...
    where winget >nul 2>&1
    if not errorlevel 1 (
        winget install --id EclipseAdoptium.Temurin.21.JRE -e --accept-source-agreements --accept-package-agreements
    ) else (
        echo [*] Lade OpenJDK 21 MSI herunter...
        powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://api.adoptium.net/v3/installer/latest/21/ga/windows/x64/jre/hotspot/normal/eclipse' -OutFile '$env:TEMP\temurin21.msi'; Start-Process msiexec.exe -ArgumentList '/i', '$env:TEMP\temurin21.msi', '/passive', '/norestart' -Wait"
    )
    echo [OK] Java 21+ Installation abgeschlossen.
)

:: ------------------------------------------------------------------------------
:: 2. Pruefen ob FFmpeg installiert ist (erforderlich fuer MP3 & Merging)
:: ------------------------------------------------------------------------------
set "FFMPEG_OK=0"
where ffmpeg >nul 2>&1
if not errorlevel 1 set "FFMPEG_OK=1"

if "%FFMPEG_OK%"=="1" (
    echo [OK] FFmpeg ist bereits installiert.
) else (
    echo [*] FFmpeg nicht gefunden. Installiere FFmpeg via winget...
    where winget >nul 2>&1
    if not errorlevel 1 (
        winget install --id Gyan.FFmpeg -e --accept-source-agreements --accept-package-agreements
        if not errorlevel 1 (
            echo [OK] FFmpeg erfolgreich installiert.
        ) else (
            echo [!] FFmpeg-Installation via winget fehlgeschlagen.
        )
    ) else (
        echo [!] Winget nicht gefunden. Bitte FFmpeg manuell installieren oder in den PATH aufnehmen.
    )
)

:: ------------------------------------------------------------------------------
:: 3. Programmdateien nach %USERPROFILE%\ytdownloader herunterladen
:: ------------------------------------------------------------------------------
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

echo [*] Lade ytdownloader und Logo herunter...
powershell -NoProfile -Command ^
    "$ProgressPreference = 'SilentlyContinue'; " ^
    "Invoke-WebRequest -Uri '%RELEASE_URL%/%JAR_NAME%' -OutFile '%INSTALL_DIR%\%JAR_NAME%'; " ^
    "Invoke-WebRequest -Uri '%RAW_URL%/ytdownloader.ico' -OutFile '%INSTALL_DIR%\ytdownloader.ico'; " ^
    "Invoke-WebRequest -Uri '%RAW_URL%/ytdownloader.png' -OutFile '%INSTALL_DIR%\ytdownloader.png'; " ^
    "Invoke-WebRequest -Uri '%RAW_URL%/uninstall.bat' -OutFile '%INSTALL_DIR%\uninstall.bat'"

if not exist "%INSTALL_DIR%\%JAR_NAME%" (
    echo [!] Fehler beim Herunterladen von %JAR_NAME%.
    pause
    exit /b 1
)

copy /y "%INSTALL_DIR%\%JAR_NAME%" "%INSTALL_DIR%\ytdownloader.jar" >nul 2>&1

:: Startskript im Zielordner anlegen
(
echo @echo off
echo cd /d "%%~dp0"
echo start "" javaw -jar "%%~dp0ytdownloader.jar" %%*
) > "%INSTALL_DIR%\run.bat"

:: ------------------------------------------------------------------------------
:: 4. Startmenue- und Desktop-Verknuepfung mit Logo erstellen
:: ------------------------------------------------------------------------------
echo [*] Erstelle Startmenue- und Desktop-Eintrag mit Logo...
powershell -NoProfile -Command ^
    "$ws = New-Object -ComObject WScript.Shell; " ^
    "$javaw = (Get-Command javaw -ErrorAction SilentlyContinue).Source; " ^
    "if (-not $javaw) { $javaw = 'javaw.exe' }; " ^
    "$smPath = Join-Path $env:APPDATA 'Microsoft\Windows\Start Menu\Programs\YouTube Downloader.lnk'; " ^
    "$dtPath = Join-Path ([Environment]::GetFolderPath('Desktop')) 'YouTube Downloader.lnk'; " ^
    "foreach ($lnkPath in @($smPath, $dtPath)) { " ^
    "  $s = $ws.CreateShortcut($lnkPath); " ^
    "  $s.TargetPath = $javaw; " ^
    "  $s.Arguments = '-jar \"' + '%INSTALL_DIR%\ytdownloader.jar' + '\"'; " ^
    "  $s.WorkingDirectory = '%INSTALL_DIR%'; " ^
    "  $s.IconLocation = '%INSTALL_DIR%\ytdownloader.ico,0'; " ^
    "  $s.Description = 'YouTube Downloader - Video and Audio Downloader'; " ^
    "  $s.Save(); " ^
    "}"

echo ==================================================================
echo   [OK] Installation erfolgreich abgeschlossen!
echo   Ordner:      %INSTALL_DIR%
echo   Startmenue:  YouTube Downloader
echo ==================================================================
pause
