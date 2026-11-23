#!/usr/bin/env bash
# ==============================================================================
# ytdownloader - Automatischer Linux Installer
# Installiert ytdownloader nach ~/ytdownloader,
# prüft/installiert Java 21+ und richtet den Desktop-Eintrag mit Logo ein.
# ==============================================================================

set -e

REPO="exitishere42/ytdownloader"
INSTALL_DIR="${INSTALL_DIR:-$HOME/ytdownloader}"
APP_NAME="ytdownloader"
JAR_NAME="ytdownloader-1.1.1-all.jar"

DETECTED_JAR=$(curl -fsSL -H "User-Agent: Mozilla/5.0" "https://api.github.com/repos/$REPO/releases/latest" 2>/dev/null | grep -o '"name": *"ytdownloader-[^"]*-all\.jar"' | head -n 1 | cut -d '"' -f 4 || true)
if [ -n "$DETECTED_JAR" ]; then
    JAR_NAME="$DETECTED_JAR"
fi

RELEASE_BASE_URL="https://github.com/$REPO/releases/latest/download"
RAW_BASE_URL="https://raw.githubusercontent.com/$REPO/main"

echo "=================================================================="
echo "  ytdownloader - Linux Installer"
echo "  Zielordner: $INSTALL_DIR"
echo "=================================================================="

# ------------------------------------------------------------------------------
# 1. Prüfen ob Java 21+ installiert ist
# ------------------------------------------------------------------------------
check_java_version() {
    if command -v java &> /dev/null; then
        local ver
        ver=$(java -version 2>&1 | head -n 1 | sed -E 's/.*"([0-9]+).*/\1/')
        if [[ "$ver" =~ ^[0-9]+$ ]] && [ "$ver" -ge 21 ]; then
            return 0
        fi
    fi
    return 1
}

if check_java_version; then
    JAVA_VER_STR=$(java -version 2>&1 | head -n 1)
    echo "[OK] Passendes Java bereits installiert ($JAVA_VER_STR) -> überspringe Java-Installation."
else
    echo "[*] Java 21+ wurde nicht gefunden. Installiere OpenJDK 21..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get update -y
        if apt-cache show openjdk-21-jre &> /dev/null; then
            sudo apt-get install -y openjdk-21-jre
        else
            echo "[*] Adoptium Repository für OpenJDK 21 hinzufügen..."
            sudo apt-get install -y wget apt-transport-https gpg
            wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
            echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list > /dev/null
            sudo apt-get update -y
            sudo apt-get install -y temurin-21-jre
        fi
    elif command -v dnf &> /dev/null; then
        sudo dnf install -y java-21-openjdk
    elif command -v pacman &> /dev/null; then
        sudo pacman -Sy --noconfirm jre-openjdk
    elif command -v zypper &> /dev/null; then
        sudo zypper install -y java-21-openjdk
    else
        echo "[!] Konnte Paketmanager nicht erkennen. Bitte OpenJDK 21+ manuell installieren."
        exit 1
    fi
    echo "[OK] Java 21+ erfolgreich installiert."
fi

# ------------------------------------------------------------------------------
# 2. Prüfen und Installieren von FFmpeg (erforderlich für MP3 und Merging)
# ------------------------------------------------------------------------------
if command -v ffmpeg &> /dev/null; then
    FFMPEG_VER_STR=$(ffmpeg -version 2>&1 | head -n 1)
    echo "[OK] FFmpeg bereits installiert ($FFMPEG_VER_STR)."
else
    echo "[*] FFmpeg wurde nicht gefunden. Installiere FFmpeg..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get update -y
        sudo apt-get install -y ffmpeg
    elif command -v dnf &> /dev/null; then
        sudo dnf install -y ffmpeg
    elif command -v pacman &> /dev/null; then
        sudo pacman -Sy --noconfirm ffmpeg
    elif command -v zypper &> /dev/null; then
        sudo zypper install -y ffmpeg
    else
        echo "[!] Paketmanager nicht erkannt. Bitte FFmpeg manuell installieren (z. B. 'sudo apt install ffmpeg')."
    fi

    if command -v ffmpeg &> /dev/null; then
        echo "[OK] FFmpeg erfolgreich installiert."
    else
        echo "[!] Warnung: FFmpeg konnte nicht automatisch installiert werden."
    fi
fi

# ------------------------------------------------------------------------------
# 3. Programmdateien in Zielordner herunterladen
# ------------------------------------------------------------------------------
echo "[*] Erstelle Programmordner: $INSTALL_DIR"
mkdir -p "$INSTALL_DIR"

download_file() {
    local url="$1"
    local dest="$2"
    if command -v curl &> /dev/null; then
        curl -fL --progress-bar -o "$dest" "$url"
    elif command -v wget &> /dev/null; then
        wget -q --show-progress -O "$dest" "$url"
    else
        echo "[!] Weder curl noch wget installiert."
        exit 1
    fi
}

echo "[*] Lade Programmdateien und Logo herunter..."
download_file "$RELEASE_BASE_URL/$JAR_NAME" "$INSTALL_DIR/$JAR_NAME"
download_file "$RAW_BASE_URL/ytdownloader.png" "$INSTALL_DIR/ytdownloader.png"
download_file "$RAW_BASE_URL/ytdownloader.svg" "$INSTALL_DIR/ytdownloader.svg"
download_file "$RAW_BASE_URL/uninstall.sh" "$INSTALL_DIR/uninstall.sh" || true
chmod +x "$INSTALL_DIR/uninstall.sh" 2>/dev/null || true

cp -f "$INSTALL_DIR/$JAR_NAME" "$INSTALL_DIR/ytdownloader.jar" 2>/dev/null || true

# Startskript run.sh erzeugen
cat > "$INSTALL_DIR/run.sh" << 'EOF'
#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"
JAR_FILE=$(ls -t "$SCRIPT_DIR"/ytdownloader-*-all.jar "$SCRIPT_DIR"/ytdownloader.jar 2>/dev/null | head -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "[!] Keine ytdownloader JAR gefunden in $SCRIPT_DIR" >&2
    exit 1
fi
exec java -jar "$JAR_FILE" "$@"
EOF
chmod +x "$INSTALL_DIR/run.sh"

# ------------------------------------------------------------------------------
# 4. Desktop-Entry & Icon für das App-Dash / Anwendungsmenü einrichten
# ------------------------------------------------------------------------------
echo "[*] Richte Desktop-Entry und App-Logo für das Dash ein..."
ICON_DIR_PNG="$HOME/.local/share/icons/hicolor/256x256/apps"
ICON_DIR_SVG="$HOME/.local/share/icons/hicolor/scalable/apps"
APPS_DIR="$HOME/.local/share/applications"

mkdir -p "$ICON_DIR_PNG" "$ICON_DIR_SVG" "$APPS_DIR"

cp "$INSTALL_DIR/ytdownloader.png" "$ICON_DIR_PNG/ytdownloader.png"
cp "$INSTALL_DIR/ytdownloader.svg" "$ICON_DIR_SVG/ytdownloader.svg"

DESKTOP_FILE="$APPS_DIR/ytdownloader.desktop"
cat > "$DESKTOP_FILE" << EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=YouTube Downloader
Comment=Fast, modern YouTube video & audio downloader
Exec=$INSTALL_DIR/run.sh
Icon=$INSTALL_DIR/ytdownloader.png
Path=$INSTALL_DIR
Terminal=false
Categories=AudioVideo;Video;Network;
StartupNotify=true
StartupWMClass=de.exit.ytdownloader.App
EOF
chmod +x "$DESKTOP_FILE"
cp "$DESKTOP_FILE" "$INSTALL_DIR/ytdownloader.desktop"

if command -v update-desktop-database &> /dev/null; then
    update-desktop-database "$APPS_DIR" &> /dev/null || true
fi
if command -v gtk-update-icon-cache &> /dev/null; then
    gtk-update-icon-cache -f -t "$HOME/.local/share/icons/hicolor" &> /dev/null || true
fi

echo "=================================================================="
echo "  [OK] Installation abgeschlossen!"
echo "  Ordner:        $INSTALL_DIR"
echo "  Startbefehl:   $INSTALL_DIR/run.sh"
echo "  Dash-Eintrag:  YouTube Downloader ist jetzt direkt im Anwendungsmenü verfügbar."
echo "=================================================================="
