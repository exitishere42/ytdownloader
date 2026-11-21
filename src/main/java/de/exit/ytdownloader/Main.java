package de.exit.ytdownloader;

/**
 * Main entry point for the application.
 * Does not extend javafx.application.Application directly to allow execution
 * from shaded Fat-JARs without module-path flags.
 */
public class Main {
    public static void main(String[] args) {
        App.main(args);
    }
}
