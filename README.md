<div align="center">

<img src="ytdownloader.png" alt="ytdownloader Logo" width="128" height="128" />

```text
         _       _                     _                 _           
 _   _  | |_  __| |  _____      ___ __ | | ___   __ _   __| | ___ _ __ 
| | | | | __|/ _` | / _ \ \ /\ / / '_ \| |/ _ \ / _` | / _` |/ _ \ '__|
| |_| | | |_| (_| || (_) \ V  V /| | | | | (_) | (_| || (_| |  __/ |   
 \__, |  \__|\__,_| \___/ \_/\_/ |_| |_|_|\___/ \__,_| \__,_|\___|_|   
 |___/                                                                 
```

**Fast, modern YouTube Video & Audio Downloader**  
*Built with Java 21 LTS & JavaFX 21*

[Features](#features) • [Installation](#installation) • [Build from Source](#build-from-source) • [Releases](https://github.com/exitishere42/ytdownloader/releases/latest)

</div>

---

## Features

- **Instant Metadata Preview**: Automatically fetches video title, channel, duration, and thumbnail preview as soon as a YouTube link is pasted.
- **Multiple Formats & Resolutions**:
  - **Video**: High-definition MP4 (Best Quality, 1080p, 720p, 480p).
  - **Audio**: High-quality MP3 audio extraction.
- **Download Queue & Parallel Execution**:
  - Add multiple videos to queue.
  - Configurable concurrent downloads.
  - Live progress bars with download speed (`MB/s`) and remaining time (`ETA`).
- **Zero-Setup Helper Bootstrap**:
  - Automatically verifies and downloads necessary helper utilities (`yt-dlp`) on first launch into the user app data directory.
  - No manual command-line installation required.
- **Cross-Platform System Integration**:
  - Native "Show in Folder" action for Windows Explorer and Linux file managers (Nautilus, Dolphin, Nemo, Thunar).
  - Desktop launcher and app menu integration with custom high-resolution logo.

---

## Installation

### Linux One-Liner
```bash
curl -fsSL https://github.com/exitishere42/ytdownloader/releases/latest/download/install.sh | bash
```

### Windows
Download and run [`install.bat`](https://github.com/exitishere42/ytdownloader/releases/latest/download/install.bat) from the [latest release](https://github.com/exitishere42/ytdownloader/releases/latest).

### Standalone Fat-JAR (Manual)
Download `ytdownloader-1.1.1-all.jar` from [Releases](https://github.com/exitishere42/ytdownloader/releases/latest) and run:
```bash
java -jar ytdownloader-1.1.1-all.jar
```

### Uninstall
- **Linux**: `curl -fsSL https://github.com/exitishere42/ytdownloader/releases/latest/download/uninstall.sh | bash`
- **Windows**: Run `uninstall.bat` in `%USERPROFILE%\ytdownloader`

---

## Build from Source

### Requirements
- **Java 21 LTS** or newer
- **Apache Maven 3.8+**

### Run Application (Development)
On Windows:
```cmd
run.bat
```

Or via Maven:
```bash
mvn javafx:run
```

### Compile & Run Tests
```bash
mvn clean test
```

### Build Standalone Executable Fat-JAR
```bash
mvn clean package
```

The standalone fat JAR is generated at:
```bash
target/ytdownloader-1.1.1-all.jar
```

---

## Author

```text
+-------------------------------------------------------------------+
|  Programmed by:     exitishere42                                  |
|  GitHub:            https://github.com/exitishere42               |
|  Repository:        https://github.com/exitishere42/ytdownloader  |
+-------------------------------------------------------------------+
```
