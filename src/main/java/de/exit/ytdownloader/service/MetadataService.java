package de.exit.ytdownloader.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.exit.ytdownloader.core.DependencyManager;
import de.exit.ytdownloader.model.VideoMetadata;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MetadataService {

    private final DependencyManager dependencyManager;
    private final ObjectMapper objectMapper;

    public MetadataService(DependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Ruft asynchron die Metadaten eines YouTube-Videos ab.
     */
    public CompletableFuture<VideoMetadata> fetchMetadataAsync(String url) {
        return CompletableFuture.supplyAsync(() -> {
            Path ytDlp = dependencyManager.findYtDlp();
            if (!dependencyManager.isYtDlpInstalled()) {
                try {
                    ytDlp = dependencyManager.ensureYtDlpAsync(null).get(60, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new IllegalStateException("yt-dlp ist noch nicht bereitgestellt: " + e.getMessage());
                }
            }

            try {
                ProcessBuilder pb = new ProcessBuilder(
                        ytDlp.toAbsolutePath().toString(),
                        "--dump-json",
                        "--no-playlist",
                        "--no-warnings",
                        "--skip-download",
                        url
                );
                pb.redirectErrorStream(true);

                Process process = pb.start();

                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }
                }

                boolean finished = process.waitFor(20, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    throw new RuntimeException("Zeitüberschreitung beim Laden der Video-Informationen.");
                }

                if (process.exitValue() != 0) {
                    throw new RuntimeException("Video konnte nicht geladen werden (ungültige URL oder geschütztes Video).");
                }

                VideoMetadata metadata = objectMapper.readValue(output.toString(), VideoMetadata.class);
                if (metadata.getWebpageUrl() == null || metadata.getWebpageUrl().isBlank()) {
                    metadata.setWebpageUrl(url);
                }
                return metadata;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }
}
