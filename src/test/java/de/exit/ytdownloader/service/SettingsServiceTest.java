package de.exit.ytdownloader.service;

import de.exit.ytdownloader.model.AppSettings;
import de.exit.ytdownloader.model.DownloadFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsServiceTest {

    @Test
    void testSettingsLifecycle() {
        SettingsService service = new SettingsService();
        AppSettings settings = service.getSettings();

        assertNotNull(settings);
        assertNotNull(settings.getDownloadDirectory());
        assertNotNull(settings.getDefaultFormat());
        assertTrue(settings.getMaxConcurrentDownloads() >= 1);

        settings.setDefaultFormat(DownloadFormat.MP3);
        service.saveSettings();

        assertEquals(DownloadFormat.MP3, service.getSettings().getDefaultFormat());
    }
}
