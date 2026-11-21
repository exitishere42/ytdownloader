#!/usr/bin/env bash
# ==============================================================================
# ytdownloader - Linux Deinstallations-Skript (Uninstaller)
# Entfernt den Programmordner (~/ytdownloader), das App-Logo und den Desktop-Entry.
# ==============================================================================

set -e

INSTALL_DIR="${INSTALL_DIR:-$HOME/ytdownloader}"
DESKTOP_FILE="$HOME/.local/share/applications/ytdownloader.desktop"
ICON_PNG="$HOME/.local/share/icons/hicolor/256x256/apps/ytdownloader.png"
ICON_SVG="$HOME/.local/share/icons/hicolor/scalable/apps/ytdownloader.svg"

echo "=================================================================="
echo "  ytdownloader - Linux Uninstaller"
echo "=================================================================="

# 1. Laufende Instanzen beenden
if pgrep -f "ytdownloader.*\.jar" &> /dev/null; then
    echo "[*] Beende laufende ytdownloader-Instanz..."
    pkill -f "ytdownloader.*\.jar" || true
    sleep 1
fi

# 2. Desktop-Entry und Icons entfernen
if [ -f "$DESKTOP_FILE" ]; then
    echo "[*] Entferne Desktop-Entry: $DESKTOP_FILE"
    rm -f "$DESKTOP_FILE"
fi

rm -f "$HOME/Desktop/ytdownloader.desktop" "$HOME/Schreibtisch/ytdownloader.desktop" 2>/dev/null || true

if [ -f "$ICON_PNG" ] || [ -f "$ICON_SVG" ]; then
    echo "[*] Entferne App-Icons..."
    rm -f "$ICON_PNG" "$ICON_SVG"
fi

if command -v update-desktop-database &> /dev/null; then
    update-desktop-database "$HOME/.local/share/applications" &> /dev/null || true
fi
if command -v gtk-update-icon-cache &> /dev/null; then
    gtk-update-icon-cache -f -t "$HOME/.local/share/icons/hicolor" &> /dev/null || true
fi

# 3. Programmordner löschen
if [ -d "$INSTALL_DIR" ]; then
    echo "[*] Lösche Programmordner: $INSTALL_DIR"
    rm -rf "$INSTALL_DIR"
else
    echo "[i] Programmordner $INSTALL_DIR existiert bereits nicht mehr."
fi

echo "=================================================================="
echo "  [OK] ytdownloader wurde vollständig deinstalliert!"
echo "=================================================================="
