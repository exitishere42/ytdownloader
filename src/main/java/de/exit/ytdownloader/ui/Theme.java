package de.exit.ytdownloader.ui;

import javafx.scene.paint.Color;

/**
 * Design-Tokens im JDMX Studio Dark Theme Stil für ytdownloader.
 */
public final class Theme {

    private Theme() {}

    // --- Dunkelstufen & Oberflächen ---
    public static final String HEX_BG_BASE = "#121212";
    public static final String HEX_SURFACE_1 = "#1A1A1A";
    public static final String HEX_SURFACE_2 = "#242424";
    public static final String HEX_SURFACE_3 = "#2D2D2D";
    public static final String HEX_BORDER = "#2E2E2E";
    public static final String HEX_BORDER_LIGHT = "#383838";

    // --- Textfarben ---
    public static final String HEX_TEXT_HIGH = "#F3F4F6";
    public static final String HEX_TEXT_MED = "#9CA3AF";
    public static final String HEX_TEXT_LOW = "#6B7280";

    // --- YouTube Red & Signalfarben ---
    public static final String HEX_PRIMARY_RED = "#DC2626";     // YouTube Red
    public static final String HEX_PRIMARY_RED_HOVER = "#B91C1C";
    public static final String HEX_PRIMARY_RED_BORDER = "#EF4444";
    public static final String HEX_RED_CONTAINER = "#381616";
    public static final String HEX_RED_CONTAINER_TEXT = "#FF8A80";

    // --- Bunte Lucide-Icon Akzente (JDMX Stil) ---
    public static final String HEX_ICON_CYAN = "#38BDF8";      // Download & Web
    public static final String HEX_ICON_AMBER = "#F59E0B";     // Fader & Settings
    public static final String HEX_ICON_GREEN = "#10B981";     // Erfolgreich / Traffic
    public static final String HEX_ICON_TEAL = "#2DD4BF";      // Ordner / I/O
    public static final String HEX_ICON_PURPLE = "#A855F7";    // Audio / Format
    public static final String HEX_ICON_RED = "#EF4444";       // Abbruch / Fehler

    // --- Color Instanzen ---
    public static final Color COLOR_TEXT_HIGH = Color.web(HEX_TEXT_HIGH);
    public static final Color COLOR_TEXT_MED = Color.web(HEX_TEXT_MED);
    public static final Color COLOR_TEXT_LOW = Color.web(HEX_TEXT_LOW);
    public static final Color COLOR_PRIMARY_RED = Color.web(HEX_PRIMARY_RED);
    public static final Color COLOR_ICON_CYAN = Color.web(HEX_ICON_CYAN);
    public static final Color COLOR_ICON_AMBER = Color.web(HEX_ICON_AMBER);
    public static final Color COLOR_ICON_GREEN = Color.web(HEX_ICON_GREEN);
    public static final Color COLOR_ICON_TEAL = Color.web(HEX_ICON_TEAL);
    public static final Color COLOR_ICON_PURPLE = Color.web(HEX_ICON_PURPLE);
    public static final Color COLOR_ICON_RED = Color.web(HEX_ICON_RED);
}
