package de.exit.ytdownloader.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProgressParser {

    // Regex für Standard yt-dlp Progress:
    // [download]  45.2% of ~  25.40MiB at    5.12MiB/s ETA 00:03
    // [download] 100% of 25.40MiB in 00:05 at 4.90MiB/s
    private static final Pattern STANDARD_PROGRESS_PATTERN = Pattern.compile(
            "\\[download\\]\\s+([0-9.]+)%\\s+of\\s+~?\\s*([0-9.]+[A-Za-z]+)(?:\\s+in\\s+([0-9:]+))?(?:\\s+at\\s+([0-9.]+[A-Za-z/]+))?(?:\\s+ETA\\s+([0-9:]+))?"
    );

    // Regex für Pipe-Format: "45.2%|5.12MiB/s|00:03"
    private static final Pattern PIPE_PROGRESS_PATTERN = Pattern.compile(
            "^\\s*([0-9.]+)%\\|([^|]*)\\|(.*)$"
    );

    public record ProgressInfo(double percentage, String speed, String eta, String stage) {
        public static ProgressInfo empty() {
            return new ProgressInfo(0.0, "", "", "");
        }
    }

    private ProgressParser() {}

    /**
     * Parst eine Zeile Konsolenausgabe von yt-dlp.
     * Gibt ein ProgressInfo zurück oder null, falls die Zeile keine Fortschrittsinformation enthält.
     */
    public static ProgressInfo parseLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String trimmed = line.trim();

        // 1. Pipe Format prüfen
        Matcher pipeMatcher = PIPE_PROGRESS_PATTERN.matcher(trimmed);
        if (pipeMatcher.find()) {
            try {
                double pct = Double.parseDouble(pipeMatcher.group(1));
                String speed = pipeMatcher.group(2).trim();
                String eta = pipeMatcher.group(3).trim();
                return new ProgressInfo(pct, speed, eta, "Lädt herunter");
            } catch (NumberFormatException ignored) {
            }
        }

        // 2. Standard yt-dlp Format prüfen
        Matcher stdMatcher = STANDARD_PROGRESS_PATTERN.matcher(trimmed);
        if (stdMatcher.find()) {
            try {
                double pct = Double.parseDouble(stdMatcher.group(1));
                String speed = stdMatcher.group(4) != null ? stdMatcher.group(4).trim() : "";
                String eta = stdMatcher.group(5) != null ? stdMatcher.group(5).trim() : "";
                return new ProgressInfo(pct, speed, eta, "Lädt herunter");
            } catch (NumberFormatException ignored) {
            }
        }

        // 3. Post-Processing & Konvertierung erkennen
        if (trimmed.startsWith("[ExtractAudio]") || trimmed.contains("Destination: ") && trimmed.endsWith(".mp3")) {
            return new ProgressInfo(100.0, "", "", "Konvertiere Audio");
        }
        if (trimmed.startsWith("[Merger]")) {
            return new ProgressInfo(100.0, "", "", "Führe Video & Audio zusammen");
        }
        if (trimmed.startsWith("[FixupM3u8]")) {
            return new ProgressInfo(99.0, "", "", "Optimiere Stream");
        }

        return null;
    }
}
