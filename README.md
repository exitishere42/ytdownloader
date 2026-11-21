# YouTube Downloader (`ytdownloader`)

A fast, reliable, cross-platform desktop application for Windows and Linux to download YouTube videos (MP4) and audio (MP3) with live metadata preview and download queue.

## Features
- **Fast Metadata Preview:** Instantly loads video title, channel, duration, and thumbnail preview.
- **Multiple Formats:** Download high-resolution videos (Best, 1080p, 720p, 480p) or extract audio (MP3).
- **Download Queue:** Manage concurrent and sequential downloads with real-time speed, ETA, and progress indicators.
- **Zero-Setup Dependency Bootstrap:** Automatically downloads and configures required helper binaries (`yt-dlp`) on Windows and Linux without manual installation.
- **Cross-Platform:** Native system integration on Windows and Linux, including direct "Show in Folder" actions.

## Prerequisites
- **Java Development Kit (JDK):** Version 21 or higher
- **Apache Maven:** Version 3.8+

## Quick Start

### Run in Development
```bash
mvn clean compile
mvn javafx:run
```

### Build Executable Fat-JAR
```bash
mvn clean package
```

The resulting standalone JAR will be located at:
```bash
target/ytdownloader-1.0.0-all.jar
```

Run the standalone application with:
```bash
java -jar target/ytdownloader-1.0.0-all.jar
```

## Running Tests
```bash
mvn test
```

## License
MIT License
