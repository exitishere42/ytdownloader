package de.exit.ytdownloader.core;

import de.exit.ytdownloader.model.DownloadFormat;
import de.exit.ytdownloader.model.DownloadStatus;
import de.exit.ytdownloader.model.DownloadTask;
import de.exit.ytdownloader.model.VideoMetadata;
import de.exit.ytdownloader.service.MetadataService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DownloadEngineTddTest {

    private static DependencyManager dependencyManager;

    @BeforeAll
    static void setup() throws Exception {
        dependencyManager = new DependencyManager();
        Path binary = dependencyManager.ensureYtDlpAsync(null).get(60, TimeUnit.SECONDS);
        assertNotNull(binary);
        assertTrue(dependencyManager.isExecutableValid(binary), "yt-dlp binary must be valid");
    }

    @Test
    void testExecutableValidationRejectsBrokenPath() {
        // Dummy/Broken File Test
        Path brokenPath = Path.of("non_existing_executable_12345.exe");
        assertFalse(dependencyManager.isExecutableValid(brokenPath));
    }

    @Test
    void testFindYtDlpFindsValidExecutable() {
        Path ytDlp = dependencyManager.findYtDlp();
        assertNotNull(ytDlp);
        assertTrue(Files.exists(ytDlp));
        assertTrue(dependencyManager.isExecutableValid(ytDlp));
        assertTrue(dependencyManager.isYtDlpInstalled());
    }

    @Test
    void testMetadataServiceFetchesRealVideoInfo() throws Exception {
        MetadataService metadataService = new MetadataService(dependencyManager);
        String testUrl = "https://www.youtube.com/watch?v=jNQXAC9IVRw";

        VideoMetadata meta = metadataService.fetchMetadataAsync(testUrl).get(30, TimeUnit.SECONDS);
        assertNotNull(meta);
        assertEquals("jNQXAC9IVRw", meta.getId());
        assertEquals("Me at the zoo", meta.getTitle());
        assertTrue(meta.getDurationSeconds() > 0);
        assertNotNull(meta.getChannel());
    }

    @Test
    void testDownloadEngineDownloadsShortVideo(@TempDir Path tempDir) throws Exception {
        DownloadEngine engine = new DownloadEngine(dependencyManager);
        String testUrl = "https://www.youtube.com/watch?v=jNQXAC9IVRw";

        DownloadTask task = new DownloadTask(testUrl, DownloadFormat.MP4_BEST);
        assertEquals(DownloadStatus.QUEUED, task.getStatus());

        engine.executeDownloadAsync(task, tempDir).get(45, TimeUnit.SECONDS);

        assertEquals(DownloadStatus.COMPLETED, task.getStatus(), "Task status must be COMPLETED");
        assertEquals(100.0, task.getProgress(), 0.1, "Progress must reach 100%");
        assertNotNull(task.getOutputPath(), "Output path must be set");
        assertTrue(Files.exists(task.getOutputPath()), "Output file must exist on disk");
        assertTrue(Files.size(task.getOutputPath()) > 50_000, "Output file must be larger than 50 KB");
    }

    @Test
    void testDownloadEngineExtractsAudioMp3(@TempDir Path tempDir) throws Exception {
        DownloadEngine engine = new DownloadEngine(dependencyManager);
        String testUrl = "https://www.youtube.com/watch?v=jNQXAC9IVRw";

        DownloadTask task = new DownloadTask(testUrl, DownloadFormat.MP3);
        assertEquals(DownloadStatus.QUEUED, task.getStatus());

        engine.executeDownloadAsync(task, tempDir).get(45, TimeUnit.SECONDS);

        assertEquals(DownloadStatus.COMPLETED, task.getStatus(), "Task status must be COMPLETED");
        assertNotNull(task.getOutputPath(), "Output path must be set");
        assertTrue(Files.exists(task.getOutputPath()), "Output file must exist on disk");
        assertTrue(task.getOutputPath().toString().endsWith(".mp3"), "File extension must be .mp3");
        assertTrue(Files.size(task.getOutputPath()) > 20_000, "Audio file must be larger than 20 KB");
    }
}
