package de.exit.ytdownloader.core;

import de.exit.ytdownloader.util.OSUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DependencyManager {

    private static final String YT_DLP_WIN_URL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
    private static final String YT_DLP_LINUX_URL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";

    private final Path binDir;

    public DependencyManager() {
        this.binDir = OSUtil.getAppDataDirectory().resolve("bin");
        try {
            if (!Files.exists(binDir)) {
                Files.createDirectories(binDir);
            }
        } catch (IOException ignored) {
        }
    }

    public Path getBinDir() {
        return binDir;
    }

    /**
     * Prüft, ob eine Datei existiert und als yt-dlp ausführbar ist (prüft --version).
     */
    public boolean isExecutableValid(Path path) {
        if (path == null || !Files.exists(path)) {
            return false;
        }
        if (!OSUtil.isWindows() && !Files.isExecutable(path)) {
            return false;
        }
        try {
            Process process = new ProcessBuilder(path.toAbsolutePath().toString(), "--version")
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(4, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            if (process.exitValue() != 0) {
                return false;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                return line != null && !line.isBlank();
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ermittelt den Pfad zur funktionierenden yt-dlp Datei.
     * Priorisiert stets den anwendungseigenen Bin-Ordner und validiert System-Kandidaten.
     */
    public Path findYtDlp() {
        String binaryName = OSUtil.isWindows() ? "yt-dlp.exe" : "yt-dlp";
        Path localBinary = binDir.resolve(binaryName);
        if (isExecutableValid(localBinary)) {
            return localBinary;
        }

        Path pathBinary = findInSystemPath(binaryName);
        if (pathBinary != null && isExecutableValid(pathBinary)) {
            return pathBinary;
        }

        return localBinary; // Zielpfad für Download
    }

    public boolean isYtDlpInstalled() {
        Path path = findYtDlp();
        return isExecutableValid(path);
    }

    /**
     * Sucht eine funktionierende Binärdatei im System-PATH.
     */
    public Path findInSystemPath(String binaryName) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isBlank()) {
            return null;
        }

        String[] parts = pathEnv.split(File.pathSeparator);
        for (String part : parts) {
            try {
                Path candidate = Path.of(part, binaryName);
                if (Files.exists(candidate) && isExecutableValid(candidate)) {
                    return candidate;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Lädt yt-dlp asynchron herunter, falls es noch nicht existiert oder defekt ist.
     */
    public CompletableFuture<Path> ensureYtDlpAsync(Consumer<Double> progressConsumer) {
        return CompletableFuture.supplyAsync(() -> {
            Path binary = findYtDlp();
            if (isExecutableValid(binary)) {
                if (progressConsumer != null) {
                    progressConsumer.accept(100.0);
                }
                return binary;
            }

            String downloadUrl = OSUtil.isWindows() ? YT_DLP_WIN_URL : YT_DLP_LINUX_URL;
            String binaryName = OSUtil.isWindows() ? "yt-dlp.exe" : "yt-dlp";
            Path targetBinary = binDir.resolve(binaryName);

            try {
                downloadFileWithRedirects(downloadUrl, targetBinary, progressConsumer);

                // Unter Linux Ausführungsrechte setzen
                if (!OSUtil.isWindows()) {
                    File file = targetBinary.toFile();
                    file.setExecutable(true, false);
                    file.setReadable(true, false);
                }

                if (!isExecutableValid(targetBinary)) {
                    throw new IOException("Heruntergeladene yt-dlp Datei konnte nicht validiert werden.");
                }

                return targetBinary;
            } catch (IOException e) {
                throw new RuntimeException("Fehler beim Herunterladen von yt-dlp: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Prüft die Versionsnummer von yt-dlp.
     */
    public String getYtDlpVersion(Path ytDlpPath) {
        if (ytDlpPath == null || !Files.exists(ytDlpPath)) {
            return null;
        }
        try {
            Process process = new ProcessBuilder(ytDlpPath.toAbsolutePath().toString(), "--version")
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                process.waitFor();
                return line != null ? line.trim() : null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void downloadFileWithRedirects(String urlString, Path targetPath, Consumer<Double> progressConsumer) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);

        int status = connection.getResponseCode();
        if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_SEE_OTHER || status == 307 || status == 308) {
            String newUrl = connection.getHeaderField("Location");
            downloadFileWithRedirects(newUrl, targetPath, progressConsumer);
            return;
        }

        if (status != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP-Status " + status + " beim Download von: " + urlString);
        }

        long contentLength = connection.getContentLengthLong();
        Path tempPath = targetPath.resolveSibling(targetPath.getFileName().toString() + ".tmp");

        try (InputStream in = connection.getInputStream()) {
            byte[] buffer = new byte[8192];
            long totalRead = 0;
            int bytesRead;

            try (var out = Files.newOutputStream(tempPath)) {
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    if (contentLength > 0 && progressConsumer != null) {
                        double percentage = (double) totalRead / contentLength * 100.0;
                        progressConsumer.accept(percentage);
                    }
                }
            }

            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            if (progressConsumer != null) {
                progressConsumer.accept(100.0);
            }
        } finally {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
            }
            connection.disconnect();
        }
    }
}
