package de.exit.ytdownloader.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public final class OSUtil {

    public enum OSType {
        WINDOWS, LINUX, MAC, OTHER
    }

    private static final OSType CURRENT_OS;

    static {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            CURRENT_OS = OSType.WINDOWS;
        } else if (osName.contains("linux")) {
            CURRENT_OS = OSType.LINUX;
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            CURRENT_OS = OSType.MAC;
        } else {
            CURRENT_OS = OSType.OTHER;
        }
    }

    private OSUtil() {}

    public static OSType getOS() {
        return CURRENT_OS;
    }

    public static boolean isWindows() {
        return CURRENT_OS == OSType.WINDOWS;
    }

    public static boolean isLinux() {
        return CURRENT_OS == OSType.LINUX;
    }

    /**
     * Liefert das persistente Anwendungsdaten-Verzeichnis:
     * - Windows: %LOCALAPPDATA%\ytdownloader
     * - Linux / Mac: ~/.local/share/ytdownloader
     */
    public static Path getAppDataDirectory() {
        Path basePath;
        if (isWindows()) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isBlank()) {
                basePath = Paths.get(localAppData);
            } else {
                basePath = Paths.get(System.getProperty("user.home"), "AppData", "Local");
            }
        } else {
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            if (xdgDataHome != null && !xdgDataHome.isBlank()) {
                basePath = Paths.get(xdgDataHome);
            } else {
                basePath = Paths.get(System.getProperty("user.home"), ".local", "share");
            }
        }

        Path appDir = basePath.resolve("ytdownloader");
        try {
            if (!Files.exists(appDir)) {
                Files.createDirectories(appDir);
            }
        } catch (IOException ignored) {
        }
        return appDir;
    }

    /**
     * Liefert den Standard-Download-Ordner des Benutzers.
     */
    public static Path getDefaultDownloadDirectory() {
        Path downloads = Paths.get(System.getProperty("user.home"), "Downloads");
        if (Files.exists(downloads)) {
            return downloads;
        }
        return Paths.get(System.getProperty("user.home"));
    }

    /**
     * Öffnet den Dateimanager und markiert die Datei bzw. öffnet das Verzeichnis plattformstabil.
     * Verwendet native OS-Prozesse (kein blockierendes Desktop.open).
     */
    public static boolean openInFileManager(Path path) {
        if (path == null) {
            return false;
        }

        try {
            if (isWindows()) {
                if (Files.isRegularFile(path)) {
                    new ProcessBuilder("explorer.exe", "/select,\"" + path.toAbsolutePath() + "\"").start();
                } else {
                    new ProcessBuilder("explorer.exe", "\"" + path.toAbsolutePath() + "\"").start();
                }
                return true;
            } else if (isLinux()) {
                Path dir = Files.isDirectory(path) ? path : path.getParent();
                if (dir == null) {
                    dir = path;
                }
                new ProcessBuilder("xdg-open", dir.toAbsolutePath().toString()).start();
                return true;
            } else {
                Path dir = Files.isDirectory(path) ? path : path.getParent();
                if (dir != null) {
                    new ProcessBuilder("open", dir.toAbsolutePath().toString()).start();
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Öffnen des Dateimanagers: " + e.getMessage());
        }
        return false;
    }
}
