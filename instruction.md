# Projektanweisung: YouTube Downloader (`ytdownloader`)

> **An den ausführenden Entwickler-Agenten:**
> Dieses Dokument ist deine verbindliche Spezifikation.
> Arbeite die Roadmap strikt Phase für Phase ab.
> Führe nach jedem Schritt die Verifikations-Befehle aus. Weiche nicht eigenmächtig von Stack oder Architektur ab.

---

## 1. Projektübersicht & Scope
- **Problem & Ziel:** Ein performantes, benutzerfreundliches und cross-platform Desktop-Tool für Windows und Linux zum schnellen Herunterladen von YouTube-Videos (MP4) und Audio-Spuren (MP3) mit Live-Vorschau und Download-Warteschlange.
- **In-Scope (MVP v1.0):**
  - **URL-Input & Metadaten-Vorschau:** Einfügen von YouTube-Links mit sofortigem Abruf von Titel, Kanal, Videodauer und Vorschaubild (Thumbnail).
  - **Zielformate:** 
    - Video: MP4 (mit Qualitätsauswahl: Beste, 1080p, 720p, 480p).
    - Audio: MP3 (High Quality Audio-Extraktion).
  - **Download-Warteschlange (Queue):** Mehrere Videos zur Queue hinzufügen; sequentieller oder paralleler Download mit Start-, Pause- und Abbruchfunktion.
  - **Echtzeit-Fortschrittsanzeige:** Live-Fortschrittsbalken, Download-Rate (z. B. `4.2 MB/s`), Restzeit (ETA) und Status.
  - **Zero-Setup Dependency Bootstrap:** Automatischer Check und Download von `yt-dlp` (sowie ggf. `ffmpeg` / `ffprobe`) in das app-eigene Datenverzeichnis, falls nicht auf dem System vorhanden.
  - **Cross-Platform OS-Integration:** Funktion "Im Ordner anzeigen", die den Download-Zielordner zuverlässig unter Windows (Explorer) und Linux (Nautilus, Dolphin, Nemo, Thunar via `xdg-open`) öffnet.
  - **Persistenz & Einstellungen:** Speichern von Zielverzeichnis, Standardformat und Download-Historie via JSON.
  - **UI-Design:** Modernes Material Design 2 Dark Theme mit gefittetem YouTube-Rot als Akzentfarbe und Lucide-Icons.
- **Out-of-Scope (Ausdrücklich verboten / Nicht implementieren):**
  - Keine Web-Server / Browser-Extensions / Cloud-Backends.
  - Keine Benutzer-Accounts, Datenbank-Server oder Login-Systeme.
  - Kein integrierter Video-Editor / Schneidetool.
  - Keine ungestylten Standard-JavaFX-Oberflächen (nur das definierte Material Design 2 Dark Theme).

---

## 2. Tech-Stack & Feste Leitplanken
- **Laufzeit & Sprache:** Java 21 LTS, modular und sauber strukturiert.
- **Build-Tool:** **Apache Maven** (`pom.xml`) – strikte Maven-Pflicht!
- **UI-Framework:** **JavaFX 21** (`javafx-controls`, `javafx-graphics`, `javafx-fxml`).
- **Core-Bibliotheken:**
  - `com.fasterxml.jackson.core:jackson-databind` (JSON-Verarbeitung für Einstellungen, Historie und `yt-dlp --dump-json`).
  - `org.junit.jupiter:junit-jupiter` (Unit- & Regressionstests).
- **Download-Engine:** `yt-dlp` als nativer Hintergrundprozess mit `--newline` Progress-Streaming.
- **Java-Spezifikation (Strikter Standard):**
  - **GroupId:** `de.exit`
  - **ArtifactId:** `ytdownloader`
  - **Base Package:** `de.exit.ytdownloader`
  - **Verzeichnisstruktur:** `src/main/java/de/exit/ytdownloader/...`
  - **Plugins:** `exec-maven-plugin` (für `mvn exec:java`), `javafx-maven-plugin`, `maven-shade-plugin` für ausführbare Fat-JARs.
- **Cross-Platform-Pflicht:** Windows 10/11 und Linux (Debian, Ubuntu mit GNOME/KDE) voll funktionsfähig.

### UI-Design & Farbvorgaben (Material Design 2 Dark Standard)
- **Hintergründe & Surfaces:**
  - Base Background: `#121212`
  - Elevated Cards / Surfaces: `#1E1E1E`
  - High Elevation (Popups, Badges, Hover): `#252525` / `#2E2E2E`
  - Divider & Ränder: `1px solid #2C2C2C`
  - Rundungen: Exakt `4px` für Karten, Buttons und Eingabefelder.
- **Primärfarbe (Harmonisiertes YouTube-Rot):**
  - Primary Accent: `#FF3B30` (Hoher Kontrast >= 4.5:1 gegen `#1E1E1E`, kein Flimmern)
  - Primary Hover: `#FF5449`
  - Primary Active/Pressed: `#D32F2F`
  - Primary Container / Soft Badge: `#381616` (mit Text `#FF8A80`)
- **Typografie & Bezeichnungen:**
  - Keine Erklärungen, Richtwerte oder Klammer-Zusätze in Optionen und Buttons.
  - Saubere Fachbezeichnungen: z. B. `"Video"`, `"Audio"`, `"Beste Qualität"`, `"1080p"`, `"720p"`, `"MP3"`, `"MP4"`, `"Herunterladen"`, `"Abbrechen"`, `"Im Ordner anzeigen"`.

### Verbotene Muster & Fallstricke (Anti-Halluzinations-Regeln)
- ❌ **Kein Gradle** – Bei Java gilt ausnahmslos Maven.
- ❌ **Kein `java.awt.Desktop.getDesktop().open()` ohne Fallbacks** – Wirft unter Linux und headless Systemen häufig Exceptions oder friert ein. Verwende immer die plattformstabile Prozess-Methode (`cmd.exe /c explorer /select,"..."` auf Windows, `xdg-open` / DBus auf Linux).
- ❌ **Kein Blockieren des JavaFX Application Threads** – Alle `yt-dlp`-Aufrufe, Metadaten-Fetches und Downloads müssen zwingend asynchron über einen dedizierten `ExecutorService` (Virtual Threads oder Background Thread Pool) laufen.
- ❌ **Keine ungefilterten Konsolenausgaben** – Der `yt-dlp` Output muss per Regex geparst werden, um Prozent, Geschwindigkeit und Restzeit sauber an das UI zu übergeben.
- ❌ **Keine hartcodierten Pfade** – Binaries und Einstellungen liegen im Standard-Nutzerverzeichnis (`%LOCALAPPDATA%\ytdownloader` bzw. `~/.local/share/ytdownloader`).

---

## 3. Architektur & Dateistruktur

```text
ytdownloader/
├── pom.xml
├── .gitignore
├── README.md
├── instruction.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── de/
    │   │       └── exit/
    │   │           └── ytdownloader/
    │   │               ├── App.java                     # JavaFX Application
    │   │               ├── Main.java                    # Entrypoint ohne JavaFX-Vererbung (Fat-JAR Safe)
    │   │               ├── core/
    │   │               │   ├── DependencyManager.java   # Download & Verifikation von yt-dlp / ffmpeg
    │   │               │   ├── DownloadEngine.java      # yt-dlp Prozesssteuerung & Progress-Parsing
    │   │               │   └── QueueManager.java        # Steuerung paralleler/sequentieller Downloads
    │   │               ├── model/
    │   │               │   ├── AppSettings.java         # Zielpfad, Standardqualität, Max. Threads
    │   │               │   ├── DownloadFormat.java      # Enum: MP4_BEST, MP4_1080P, MP4_720P, MP3
    │   │               │   ├── DownloadStatus.java      # Enum: QUEUED, FETCHING, DOWNLOADING, etc.
    │   │               │   ├── DownloadTask.java        # Entity für ein Video in der Queue
    │   │               │   └── VideoMetadata.java       # Metadaten-Objekt (Titel, Thumbnail, Dauer)
    │   │               ├── service/
    │   │               │   ├── MetadataService.java     # Schneller Metadaten-Abruf via yt-dlp --dump-json
    │   │               │   └── SettingsService.java     # Laden & Speichern der JSON-Konfiguration
    │   │               ├── ui/
    │   │               │   ├── MainWindow.java          # Hauptansicht (JavaFX Layout)
    │   │               │   ├── QueueView.java           # Listen-Komponente für aktive & beendete Downloads
    │   │               │   ├── TaskCard.java            # Material 2 Card für einen Download
    │   │               │   └── Theme.java               # CSS / Style-Konstanten (Dark Theme & Red Accent)
    │   │               └── util/
    │   │                   ├── LucideIcon.java          # SVG/Path-Icons für Material Design UI
    │   │                   ├── OSUtil.java              # Erkennung Windows / Linux & Dateimanager-Aufruf
    │   │                   └── TimeUtil.java            # Formatierung von Sekunden in hh:mm:ss
    │   └── resources/
    │       └── de/
    │           └── exit/
    │               └── ytdownloader/
    │                   └── style.css                    # Material Design 2 Dark Stylesheet
    └── test/
        └── java/
            └── de/
                └── exit/
                    └── ytdownloader/
                        ├── core/
                        │   └── ProgressParserTest.java  # Regex- & Progress-Parsing Tests
                        ├── model/
                        │   └── DownloadTaskTest.java    # Status- & Transition-Tests
                        └── util/
                            └── OSUtilTest.java          # OS-Erkennung & Pfad-Tests
```

---

## 4. Datenmodelle & Schemas

### DownloadTask
```java
public class DownloadTask {
    private final String id;
    private final String url;
    private String title;
    private String channel;
    private long durationSeconds;
    private String thumbnailUrl;
    private DownloadFormat format;
    private DownloadStatus status;
    private double progressPercentage; // 0.0 bis 100.0
    private String speed;              // z. B. "5.2 MiB/s"
    private String eta;                // z. B. "00:45"
    private Path outputPath;
    private String errorMessage;
    private final Instant createdAt;
    // Getters, Setters, Status-Transitions
}
```

### AppSettings
```java
public class AppSettings {
    private String downloadDirectory;       // Default: OS Downloads-Ordner
    private DownloadFormat defaultFormat;    // Default: MP4_BEST
    private int maxConcurrentDownloads;      // Default: 2
    private String customYtDlpPath;         // Optional: Eigene Binary
    private String customFfmpegPath;        // Optional: Eigene Binary
}
```

### DownloadFormat (Enum)
```java
public enum DownloadFormat {
    MP4_BEST("Video (Beste Qualität)", "bestvideo+bestaudio/best", "mp4"),
    MP4_1080P("Video (1080p)", "bestvideo[height<=1080]+bestaudio/best[height<=1080]", "mp4"),
    MP4_720P("Video (720p)", "bestvideo[height<=720]+bestaudio/best[height<=720]", "mp4"),
    MP3("Audio (MP3)", "bestaudio/best", "mp3");

    private final String label;
    private final String formatSpec;
    private final String extension;
    // ...
}
```

---

## 5. Detaillierte Features mit "Definition of Done" (DoD)

### Feature 1: Zero-Setup Dependency Bootstrap
- **Beschreibung:** Beim ersten Start prüft die App, ob `yt-dlp` verfügbar ist. Wenn weder im System-PATH noch im App-Datenordner vorhanden, wird die offizielle aktuelle Binary für das jeweilige Betriebssystem (Windows: `.exe`, Linux: Standalone-Binary mit `chmod +x`) sicher heruntergeladen.
- **Definition of Done:**
  - *Prüfung:* `DependencyManager.ensureDependencies()` liefert ein gültiges und ausführbares `Path`-Objekt für `yt-dlp`.
  - *Ausführung:* `yt-dlp --version` gibt einen Exit-Code 0 und die Versionsnummer zurück.
  - *Zustand:* Datei liegt im persistenten Datenordner (`%LOCALAPPDATA%\ytdownloader\bin\` oder `~/.local/share/ytdownloader/bin/`).

### Feature 2: Schnelle Metadaten-Vorschau
- **Beschreibung:** Nach Eingabe oder Einfügen einer gültigen YouTube-URL (Video oder Kurzlink) wird asynchron eine Vorschaukarte mit Thumbnail, Videotitel, Kanal und Dauer geladen.
- **Definition of Done:**
  - *Eingabe:* `https://www.youtube.com/watch?v=...` im Eingabefeld.
  - *Erwartetes Ergebnis:* UI zeigt Lade-Spinner, danach Titel, Uploader und Thumbnail-Bild in einer Material 2 Card.
  - *Fehlertoleranz:* Bei ungültiger URL erscheint ein sauberer Fehlerhinweis ohne Absturz.

### Feature 3: Download-Warteschlange & Progress-Tracking
- **Beschreibung:** Klick auf "Zur Queue hinzufügen" oder "Herunterladen" reiht den Task ein. Der `QueueManager` führt Tasks aus und parst kontinuierlich den `yt-dlp` Konsolenoutput.
- **Definition of Done:**
  - *Eingabe:* Klick auf "Herunterladen".
  - *Erwartetes Ergebnis:* Status wechselt auf `DOWNLOADING`. ProgressBar aktualisiert sich flüssig. Geschwindigkeit und Restzeit werden exakt angezeigt.
  - *Ausgabe:* Nach Abschluss Status `FERTIG`, Button "Im Ordner anzeigen" wird aktiv. Datei liegt im Zielordner mit korrekter Dateiendung.

### Feature 4: Plattformstabiles "Im Ordner anzeigen"
- **Beschreibung:** Öffnet den Ordner und selektiert die heruntergeladene Datei im nativen Dateimanager.
- **Definition of Done:**
  - *Windows:* `explorer.exe /select,"C:\path\to\file.mp4"` öffnet den Ordner mit markierter Datei.
  - *Linux:* `xdg-open "/path/to/folder"` öffnet den Datei-Manager.
  - *Fehlertoleranz:* Keine `AWTException` oder Hänger.

---

## 6. Fehlermatrix & Edge Cases

| Situation / Ausnahme | Systemverhalten | Feedback an den Nutzer |
| :--- | :--- | :--- |
| Ungültige YouTube-URL | Validierung schlägt fehl, kein Prozessstart | Rote Umrandung & Toast: "Ungültige YouTube-URL" |
| Keine Internetverbindung | Socket-Timeout bei Metadaten / Download | "Keine Internetverbindung verfügbar" |
| Video privat / gelöscht | yt-dlp gibt Fehlercode 1 aus | Fehlermeldung: "Video nicht verfügbar oder privat" |
| Fehlender Speicherplatz | yt-dlp IOError / Write Error | "Nicht genügend freier Speicherplatz auf dem Ziellaufwerk" |
| Download abgebrochen | Prozess wird sauber beendet (`destroyForcibly()`), temporäre `.part`-Datei gelöscht | Status `ABGEBROCHEN` in der Queue |
| yt-dlp veraltet | Update-Befehl oder automatischer Re-Download | Info-Meldung mit automatischer Update-Möglichkeit |

---

## 7. Phasenweise Implementierungs-Roadmap

### Phase 1: Projekt-Setup & Infrastruktur
- [ ] Maven `pom.xml` mit Java 21, JavaFX 21, Jackson, JUnit 5 und Build-Plugins konfigurieren.
- [ ] Basispakete anlegen: `de.exit.ytdownloader`.
- [ ] `Main.java` und `App.java` als sauberen Einstiegspunkt für JavaFX und Fat-JAR einrichten.
- [ ] **Verifikation:** `mvn clean compile` läuft fehlerfrei durch.

### Phase 2: Core-Engine & Dependency-Bootstrap
- [ ] `OSUtil` zur Erkennung des Betriebssystems und der Pfade erstellen.
- [ ] `DependencyManager` für den automatischen Download von `yt-dlp` (Windows & Linux) implementieren.
- [ ] `DownloadEngine` für den asynchronen Prozessstart und Regex-Parsing des Outputs bauen.
- [ ] Unit-Tests für `ProgressParser` und `OSUtil` schreiben.
- [ ] **Verifikation:** `mvn test` läuft zu 100 % erfolgreich durch.

### Phase 3: Datenmodelle & Queue-Management
- [ ] `DownloadTask`, `DownloadStatus`, `DownloadFormat` und `AppSettings` definieren.
- [ ] `SettingsService` zur JSON-Persistenz der Benutzereinstellungen implementieren.
- [ ] `QueueManager` zur Verwaltung von parallelen Downloads (Semaphore / ExecutorService) bauen.
- [ ] **Verifikation:** Unit-Tests für Queue-Statuswechsel und JSON-Serialisierung bestehen.

### Phase 4: Benutzeroberfläche (Material Design 2 Dark)
- [ ] `style.css` und `Theme.java` mit dunklen Oberflächen (`#121212`, `#1E1E1E`, `#252525`) und gefittetem YouTube-Rot (`#FF3B30`) aufbauen.
- [ ] URL-Eingabebar mit "Einfügen"-Button und Format-Dropdowns (ohne Klammer-Erklärungen) gestalten.
- [ ] Metadaten-Vorschaukarte mit Thumbnail-Anzeige implementieren.
- [ ] `QueueView` und `TaskCard` mit Fortschrittsbalken, Geschwindigkeit, ETA und Aktions-Buttons (Pause, Stop, Ordner) umsetzen.
- [ ] **Verifikation:** `mvn javafx:run` startet die GUI mit voll funktionsfähigem Design.

### Phase 5: End-to-End Tests & Packaging
- [ ] Vollständigen Test-Download (Video MP4 & Audio MP3) durchführen.
- [ ] Fat-JAR Build über `maven-shade-plugin` konfigurieren und prüfen.
- [ ] README.md mit Kurzanleitung und Screenshots/Features erstellen.
- [ ] **Verifikation:** `mvn clean package` erzeugt eine eigenständig startbare Fat-JAR unter `target/ytdownloader-1.0.0-all.jar`.

---

## 8. Start- und Build-Befehle
- **Kompilieren:** `mvn clean compile`
- **Tests ausführen:** `mvn test`
- **In Entwicklungsumgebung ausführen:** `mvn javafx:run`
- **Fat-JAR Paketieren:** `mvn clean package`
- **Fertige JAR starten:** `java -jar target/ytdownloader-1.0.0-all.jar`
