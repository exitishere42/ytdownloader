package de.exit.ytdownloader.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DownloadTaskTest {

    @Test
    void testTaskInitialization() {
        DownloadTask task = new DownloadTask("https://www.youtube.com/watch?v=dQw4w9WgXcQ", DownloadFormat.MP4_BEST);

        assertNotNull(task.getId());
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", task.getUrl());
        assertEquals(DownloadStatus.QUEUED, task.getStatus());
        assertEquals(0.0, task.getProgress());
        assertEquals(DownloadFormat.MP4_BEST, task.getFormat());
    }

    @Test
    void testMetadataAssignment() {
        DownloadTask task = new DownloadTask("https://www.youtube.com/watch?v=test", DownloadFormat.MP3);
        VideoMetadata meta = new VideoMetadata("test", "Test Title", "Test Channel", 185, "https://thumb.jpg", "https://www.youtube.com/watch?v=test");

        task.setMetadata(meta);

        assertEquals("Test Title", task.getTitle());
        assertEquals("Test Channel", task.getChannel());
        assertEquals(185, task.getDurationSeconds());
        assertEquals("https://thumb.jpg", task.getThumbnailUrl());
    }

    @Test
    void testProgressUpdate() {
        DownloadTask task = new DownloadTask("https://www.youtube.com/watch?v=test", DownloadFormat.MP4_BEST);
        task.updateProgress(55.5, "10.2 MiB/s", "00:10");

        assertEquals(55.5, task.getProgress(), 0.001);
        assertEquals("10.2 MiB/s", task.getSpeed());
        assertEquals("00:10", task.getEta());
    }

    @Test
    void testStatusTransitions() {
        DownloadTask task = new DownloadTask("https://www.youtube.com/watch?v=test", DownloadFormat.MP4_BEST);
        task.setStatus(DownloadStatus.DOWNLOADING);
        assertTrue(task.getStatus().isActive());
        assertFalse(task.getStatus().isTerminal());

        task.setStatus(DownloadStatus.COMPLETED);
        assertTrue(task.getStatus().isTerminal());
        assertFalse(task.getStatus().isActive());
    }
}
