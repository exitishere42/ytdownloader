package de.exit.ytdownloader.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für den Update-Dienst {@link UpdateService}.
 */
class UpdateServiceTest {

    @Test
    @DisplayName("Sollte SemVer-Versionen präzise vergleichen")
    void shouldCompareVersionsCorrectly() {
        // Neuere Versionen
        assertTrue(UpdateService.isVersionNewer("v1.2.1", "1.2.0"));
        assertTrue(UpdateService.isVersionNewer("1.3.0", "1.2.0"));
        assertTrue(UpdateService.isVersionNewer("v2.0.0", "1.2.0"));
        assertTrue(UpdateService.isVersionNewer("1.2.0.1", "1.2.0"));
        assertTrue(UpdateService.isVersionNewer("v1.1.2", "1.1.1"));

        // Ältere oder gleiche Versionen
        assertFalse(UpdateService.isVersionNewer("1.2.0", "1.2.0"));
        assertFalse(UpdateService.isVersionNewer("v1.2.0", "1.2.0"));
        assertFalse(UpdateService.isVersionNewer("v1.1.9", "1.2.0"));
        assertFalse(UpdateService.isVersionNewer("1.0.0", "1.2.0"));
        assertFalse(UpdateService.isVersionNewer("v0.9.0", "1.2.0"));
        assertFalse(UpdateService.isVersionNewer("v1.1.1", "1.1.1"));
        assertFalse(UpdateService.isVersionNewer("v1.1.0", "1.1.1"));

        // Null-Safety & ungültige Eingaben
        assertFalse(UpdateService.isVersionNewer(null, "1.2.0"));
        assertFalse(UpdateService.isVersionNewer("v1.2.0", null));
        assertFalse(UpdateService.isVersionNewer(null, null));
        assertFalse(UpdateService.isVersionNewer("", ""));
    }

    @Test
    @DisplayName("Sollte korrekte Konstanten für Repository und Version besitzen")
    void shouldHaveValidRepoAndVersionConstants() {
        assertNotNull(UpdateService.CURRENT_VERSION);
        assertTrue(UpdateService.CURRENT_VERSION.matches("^\\d+\\.\\d+\\.\\d+.*$"), "Version must follow SemVer pattern");
        assertEquals("exitishere42/ytdownloader", UpdateService.GITHUB_REPO);
        assertEquals("https://api.github.com/repos/exitishere42/ytdownloader/releases/latest", UpdateService.API_URL);
        assertEquals("https://github.com/exitishere42/ytdownloader", UpdateService.REPO_URL);
    }
}
