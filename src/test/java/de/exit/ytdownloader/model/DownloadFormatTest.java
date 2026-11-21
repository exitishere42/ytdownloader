package de.exit.ytdownloader.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DownloadFormatTest {

    @Test
    void testMp4BestArguments() {
        DownloadFormat format = DownloadFormat.MP4_BEST;
        List<String> args = format.buildYtDlpArguments();

        assertTrue(args.contains("-f"));
        assertTrue(args.contains("bestvideo+bestaudio/best"));
        assertTrue(args.contains("--merge-output-format"));
        assertTrue(args.contains("mp4"));
        assertFalse(format.isAudioOnly());
    }

    @Test
    void testMp3AudioArguments() {
        DownloadFormat format = DownloadFormat.MP3;
        List<String> args = format.buildYtDlpArguments();

        assertTrue(args.contains("-x"));
        assertTrue(args.contains("--audio-format"));
        assertTrue(args.contains("mp3"));
        assertTrue(format.isAudioOnly());
    }

    @Test
    void testResolutions() {
        assertEquals("mp4", DownloadFormat.MP4_1080P.getFileExtension());
        assertEquals("mp4", DownloadFormat.MP4_720P.getFileExtension());
        assertEquals("mp3", DownloadFormat.MP3.getFileExtension());
    }
}
