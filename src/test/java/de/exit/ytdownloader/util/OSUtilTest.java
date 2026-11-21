package de.exit.ytdownloader.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OSUtilTest {

    @Test
    void testOSDetection() {
        assertNotNull(OSUtil.getOS());
    }

    @Test
    void testAppDataDirectoryNotNull() {
        Path appData = OSUtil.getAppDataDirectory();
        assertNotNull(appData);
        assertTrue(appData.toString().contains("ytdownloader"));
    }

    @Test
    void testDefaultDownloadDirectoryNotNull() {
        Path downloads = OSUtil.getDefaultDownloadDirectory();
        assertNotNull(downloads);
    }
}
