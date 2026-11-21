#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ -f "$SCRIPT_DIR/target/ytdownloader-1.0.0-all.jar" ]; then
    exec java -jar "$SCRIPT_DIR/target/ytdownloader-1.0.0-all.jar" "$@"
elif [ -f "$SCRIPT_DIR/ytdownloader.jar" ]; then
    exec java -jar "$SCRIPT_DIR/ytdownloader.jar" "$@"
else
    exec mvn javafx:run
fi
