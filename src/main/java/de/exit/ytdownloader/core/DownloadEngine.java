package de.exit.ytdownloader.core;

import de.exit.ytdownloader.model.DownloadFormat;
import de.exit.ytdownloader.model.DownloadStatus;
import de.exit.ytdownloader.model.DownloadTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DownloadEngine {

    private final DependencyManager dependencyManager;

    public DownloadEngine(DependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
    }

    public CompletableFuture<Void> executeDownloadAsync(DownloadTask task, Path outputDirectory) {
        return CompletableFuture.runAsync(() -> {
            DownloadFormat format = task.getFormat();
            if (format.isAudioOnly() && !dependencyManager.isFfmpegInstalled()) {
                task.setStatus(DownloadStatus.FAILED);
                task.setErrorMessage("FFmpeg nicht gefunden! Für Audio-Konvertierung (MP3) wird FFmpeg benötigt. Bitte installieren Sie FFmpeg (z. B. via 'install.sh' / 'install.bat' oder 'sudo apt install ffmpeg').");
                return;
            }

            Path ytDlp = dependencyManager.findYtDlp();
            if (!dependencyManager.isYtDlpInstalled()) {
                try {
                    ytDlp = dependencyManager.ensureYtDlpAsync(null).get(60, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception e) {
                    task.setStatus(DownloadStatus.FAILED);
                    task.setErrorMessage("yt-dlp nicht verfügbar: " + e.getMessage());
                    return;
                }
            }

            try {
                if (!Files.exists(outputDirectory)) {
                    Files.createDirectories(outputDirectory);
                }

                task.setStatus(DownloadStatus.DOWNLOADING);

                List<String> command = new ArrayList<>();
                command.add(ytDlp.toAbsolutePath().toString());
                command.add("--newline");
                command.add("--no-colors");
                command.add("--no-playlist");

                // Output-Template
                String outputTemplate = outputDirectory.resolve("%(title)s.%(ext)s").toAbsolutePath().toString();
                command.add("-o");
                command.add(outputTemplate);

                // Format-Argumente
                Path ffmpegPath = dependencyManager.findFfmpeg();
                if (ffmpegPath != null) {
                    command.add("--ffmpeg-location");
                    command.add(ffmpegPath.toAbsolutePath().toString());
                }

                command.addAll(format.buildYtDlpArguments());

                // URL
                command.add(task.getUrl());

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);

                Process process = pb.start();
                task.setProcess(process);

                Path resolvedOutputFile = null;
                List<String> outputLines = new ArrayList<>();
                List<String> errorLines = new ArrayList<>();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty()) {
                            continue;
                        }

                        outputLines.add(trimmed);
                        if (trimmed.startsWith("ERROR:") || trimmed.toLowerCase().contains("error:") || trimmed.startsWith("[download] Got error:")) {
                            errorLines.add(trimmed);
                        }

                        // Dateinamen erfassen
                        if (trimmed.startsWith("[download] Destination: ")) {
                            resolvedOutputFile = Paths.get(trimmed.substring("[download] Destination: ".length()).trim());
                        } else if (trimmed.startsWith("[Merger] Merging formats into \"")) {
                            String pathStr = trimmed.substring("[Merger] Merging formats into \"".length());
                            if (pathStr.endsWith("\"")) {
                                pathStr = pathStr.substring(0, pathStr.length() - 1);
                            }
                            resolvedOutputFile = Paths.get(pathStr);
                        } else if (trimmed.startsWith("[ExtractAudio] Destination: ")) {
                            resolvedOutputFile = Paths.get(trimmed.substring("[ExtractAudio] Destination: ".length()).trim());
                        }

                        // Fortschritt parsen
                        ProgressParser.ProgressInfo info = ProgressParser.parseLine(trimmed);
                        if (info != null) {
                            task.updateProgress(info.percentage(), info.speed(), info.eta());
                        }
                    }
                }

                int exitCode = process.waitFor();
                if (task.getStatus() == DownloadStatus.CANCELLED) {
                    return;
                }

                if (exitCode == 0) {
                    task.updateProgress(100.0, "", "");
                    task.setStatus(DownloadStatus.COMPLETED);
                    if (resolvedOutputFile != null) {
                        task.setOutputPath(resolvedOutputFile);
                    } else {
                        // Fallback: Suche Datei im Ausgabeverzeichnis
                        task.setOutputPath(outputDirectory);
                    }
                } else {
                    task.setStatus(DownloadStatus.FAILED);
                    String detail;
                    if (!errorLines.isEmpty()) {
                        detail = String.join(" | ", errorLines);
                    } else if (!outputLines.isEmpty()) {
                        int start = Math.max(0, outputLines.size() - 3);
                        detail = String.join(" | ", outputLines.subList(start, outputLines.size()));
                    } else {
                        detail = "Exit-Code: " + exitCode;
                    }
                    task.setErrorMessage("Download fehlgeschlagen (Exit-Code " + exitCode + "): " + detail);
                }
            } catch (InterruptedException e) {
                task.cancel();
            } catch (Exception e) {
                task.setStatus(DownloadStatus.FAILED);
                task.setErrorMessage(e.getMessage());
            } finally {
                task.setProcess(null);
            }
        });
    }
}
