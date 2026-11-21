package de.exit.ytdownloader.model;

public enum DownloadStatus {
    QUEUED("Warteschlange"),
    FETCHING("Lade Informationen"),
    DOWNLOADING("Lädt herunter"),
    CONVERTING("Konvertiert"),
    COMPLETED("Fertig"),
    FAILED("Fehler"),
    CANCELLED("Abgebrochen");

    private final String displayName;

    DownloadStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean isActive() {
        return this == FETCHING || this == DOWNLOADING || this == CONVERTING;
    }
}
