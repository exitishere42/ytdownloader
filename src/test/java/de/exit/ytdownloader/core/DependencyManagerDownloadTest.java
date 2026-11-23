package de.exit.ytdownloader.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DependencyManagerDownloadTest {

    @Test
    void testDownloadYtDlp() throws Exception {
        DependencyManager manager = new DependencyManager();
        Path ytDlp = manager.findYtDlp();
        System.out.println("findYtDlp: " + ytDlp + " exists: " + Files.exists(ytDlp));
        System.out.println("binDir: " + manager.getBinDir() + " exists: " + Files.exists(manager.getBinDir()));

        Path downloaded = manager.ensureYtDlpAsync(pct -> {
            System.out.printf("Download progress: %.1f%%%n", pct);
        }).get(60, TimeUnit.SECONDS);

        System.out.println("downloaded: " + downloaded + " exists: " + Files.exists(downloaded) + " size: " + (Files.exists(downloaded) ? Files.size(downloaded) : -1));

        assertNotNull(downloaded);
        assertTrue(Files.exists(downloaded), "yt-dlp binary must exist on disk");
        assertTrue(manager.isYtDlpInstalled(), "isYtDlpInstalled must return true");

        String version = manager.getYtDlpVersion(downloaded);
        System.out.println("yt-dlp version: " + version);
        assertNotNull(version, "Version must not be null");
        assertFalse(version.isBlank(), "Version must not be blank");
    }

    @Test
    void testFfmpegValidationRejectsBrokenPath() {
        DependencyManager manager = new DependencyManager();
        assertFalse(manager.isFfmpegValid(Path.of("invalid_ffmpeg_path_98765.exe")));
    }

    @Test
    void testFindFfmpeg() {
        DependencyManager manager = new DependencyManager();
        Path ffmpeg = manager.findFfmpeg();
        System.out.println("findFfmpeg: " + ffmpeg + " isInstalled: " + manager.isFfmpegInstalled());
        if (ffmpeg != null) {
            assertTrue(Files.exists(ffmpeg));
            assertTrue(manager.isFfmpegValid(ffmpeg));
        }
    }
}
