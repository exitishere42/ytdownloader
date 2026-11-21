package de.exit.ytdownloader.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgressParserTest {

    @Test
    void testStandardProgressParsing() {
        String line = "[download]  45.2% of ~  25.40MiB at    5.12MiB/s ETA 00:03";
        ProgressParser.ProgressInfo info = ProgressParser.parseLine(line);

        assertNotNull(info);
        assertEquals(45.2, info.percentage(), 0.001);
        assertEquals("5.12MiB/s", info.speed());
        assertEquals("00:03", info.eta());
        assertEquals("Lädt herunter", info.stage());
    }

    @Test
    void testStandardFinishedProgress() {
        String line = "[download] 100% of 25.40MiB in 00:05 at 4.90MiB/s";
        ProgressParser.ProgressInfo info = ProgressParser.parseLine(line);

        assertNotNull(info);
        assertEquals(100.0, info.percentage(), 0.001);
        assertEquals("4.90MiB/s", info.speed());
    }

    @Test
    void testPipeProgressParsing() {
        String line = " 72.8%|12.4MiB/s|00:01";
        ProgressParser.ProgressInfo info = ProgressParser.parseLine(line);

        assertNotNull(info);
        assertEquals(72.8, info.percentage(), 0.001);
        assertEquals("12.4MiB/s", info.speed());
        assertEquals("00:01", info.eta());
    }

    @Test
    void testExtractAudioDetection() {
        String line = "[ExtractAudio] Destination: test.mp3";
        ProgressParser.ProgressInfo info = ProgressParser.parseLine(line);

        assertNotNull(info);
        assertEquals("Konvertiere Audio", info.stage());
    }

    @Test
    void testMergerDetection() {
        String line = "[Merger] Merging formats into \"test.mp4\"";
        ProgressParser.ProgressInfo info = ProgressParser.parseLine(line);

        assertNotNull(info);
        assertEquals("Führe Video & Audio zusammen", info.stage());
    }

    @Test
    void testIrrelevantLinesReturnNull() {
        assertNull(ProgressParser.parseLine("[youtube] Extracting URL: https://..."));
        assertNull(ProgressParser.parseLine(""));
        assertNull(ProgressParser.parseLine(null));
    }
}
