package de.exit.ytdownloader.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.exit.ytdownloader.model.AppSettings;
import de.exit.ytdownloader.util.OSUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsService {

    private final Path settingsFile;
    private final ObjectMapper mapper;
    private AppSettings cachedSettings;

    public SettingsService() {
        this.settingsFile = OSUtil.getAppDataDirectory().resolve("settings.json");
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public synchronized AppSettings getSettings() {
        if (cachedSettings == null) {
            loadSettings();
        }
        return cachedSettings;
    }

    public synchronized void loadSettings() {
        if (Files.exists(settingsFile)) {
            try {
                this.cachedSettings = mapper.readValue(settingsFile.toFile(), AppSettings.class);
                return;
            } catch (Exception e) {
                System.err.println("Konnte settings.json nicht laden, erstelle Standardeinstellungen: " + e.getMessage());
            }
        }
        this.cachedSettings = new AppSettings();
        saveSettings();
    }

    public synchronized void saveSettings() {
        if (cachedSettings == null) {
            cachedSettings = new AppSettings();
        }
        try {
            if (!Files.exists(settingsFile.getParent())) {
                Files.createDirectories(settingsFile.getParent());
            }
            mapper.writeValue(settingsFile.toFile(), cachedSettings);
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Einstellungen: " + e.getMessage());
        }
    }
}
