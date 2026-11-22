package de.exit.ytdownloader.ui;

import de.exit.ytdownloader.core.DependencyManager;
import de.exit.ytdownloader.model.AppSettings;
import de.exit.ytdownloader.model.DownloadFormat;
import de.exit.ytdownloader.service.SettingsService;
import de.exit.ytdownloader.util.LucideIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public class SettingsOverlay extends StackPane {

    private final SettingsService settingsService;
    private final DependencyManager dependencyManager;
    private final Runnable onCloseCallback;

    public SettingsOverlay(SettingsService settingsService, DependencyManager dependencyManager, Runnable onCloseCallback) {
        this.settingsService = settingsService;
        this.dependencyManager = dependencyManager;
        this.onCloseCallback = onCloseCallback;

        getStyleClass().add("modal-overlay-scrim");
        setVisible(false);
        setManaged(false);

        AppSettings settings = settingsService.getSettings();

        // Dialog Card
        VBox card = new VBox(16);
        card.getStyleClass().add("settings-dialog-card");
        card.setMaxWidth(480);
        card.setMaxHeight(400);

        // 1. Header Row
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Einstellungen");
        title.getStyleClass().add("dialog-title");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        closeBtn.setGraphic(LucideIcon.createIcon(LucideIcon.CANCEL, Theme.COLOR_TEXT_MED, 0.8));
        closeBtn.getStyleClass().add("dialog-close-btn");
        closeBtn.setOnAction(e -> hide());

        headerRow.getChildren().addAll(title, spacer, closeBtn);

        // 2. Download-Verzeichnis
        VBox dirSection = new VBox(6);
        Label dirLabel = new Label("Download-Verzeichnis");
        dirLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-weight: 600;");

        HBox dirInputRow = new HBox(8);
        dirInputRow.setAlignment(Pos.CENTER_LEFT);

        HBox searchBox = new HBox(8);
        searchBox.getStyleClass().add("search-box");
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        TextField dirField = new TextField(settings.getDownloadDirectory());
        dirField.setEditable(false);
        dirField.getStyleClass().add("search-field");
        HBox.setHgrow(dirField, Priority.ALWAYS);
        searchBox.getChildren().addAll(LucideIcon.createIcon(LucideIcon.FOLDER, Theme.COLOR_ICON_TEAL, 0.8), dirField);

        Button browseBtn = new Button("Durchsuchen");
        browseBtn.getStyleClass().add("btn-secondary");
        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Download-Ordner wählen");
            File initial = new File(settings.getDownloadDirectory());
            if (initial.exists()) {
                chooser.setInitialDirectory(initial);
            }
            Window window = getScene() != null ? getScene().getWindow() : null;
            File selected = chooser.showDialog(window);
            if (selected != null) {
                dirField.setText(selected.getAbsolutePath());
            }
        });

        dirInputRow.getChildren().addAll(searchBox, browseBtn);
        dirSection.getChildren().addAll(dirLabel, dirInputRow);

        // 3. Standard-Format
        VBox formatSection = new VBox(6);
        Label formatLabel = new Label("Standard-Format");
        formatLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-weight: 600;");

        ComboBox<DownloadFormat> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll(DownloadFormat.values());
        formatCombo.setValue(settings.getDefaultFormat());
        formatCombo.setMaxWidth(Double.MAX_VALUE);
        formatSection.getChildren().addAll(formatLabel, formatCombo);

        // 4. Parallele Downloads
        VBox threadSection = new VBox(6);
        Label threadLabel = new Label("Gleichzeitige Downloads");
        threadLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-weight: 600;");

        Spinner<Integer> threadSpinner = new Spinner<>(1, 5, settings.getMaxConcurrentDownloads());
        threadSpinner.setMaxWidth(Double.MAX_VALUE);
        threadSection.getChildren().addAll(threadLabel, threadSpinner);

        // 5. Engine Status Pill
        HBox engineInfoRow = new HBox(10);
        engineInfoRow.setAlignment(Pos.CENTER_LEFT);
        engineInfoRow.setPadding(new Insets(6, 0, 0, 0));

        Label engineDot = new Label("●");
        boolean ready = dependencyManager.isYtDlpInstalled();
        engineDot.setStyle("-fx-text-fill: " + (ready ? Theme.HEX_ICON_GREEN : Theme.HEX_ICON_AMBER) + "; -fx-font-size: 12px;");

        String version = ready ? dependencyManager.getYtDlpVersion(dependencyManager.findYtDlp()) : "Wird geladen...";
        Label engineLabel = new Label("yt-dlp Engine: " + (version != null ? version : "Bereit"));
        engineLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        engineInfoRow.getChildren().addAll(engineDot, engineLabel);

        // 6. Action Button Bar
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Abbrechen");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> hide());

        Button saveBtn = new Button("Speichern");
        saveBtn.getStyleClass().add("btn-primary-action");
        saveBtn.setOnAction(e -> {
            settings.setDownloadDirectory(dirField.getText());
            settings.setDefaultFormat(formatCombo.getValue());
            settings.setMaxConcurrentDownloads(threadSpinner.getValue());
            settingsService.saveSettings();
            hide();
        });

        buttonBar.getChildren().addAll(cancelBtn, saveBtn);

        card.getChildren().addAll(headerRow, dirSection, formatSection, threadSection, engineInfoRow, buttonBar);
        getChildren().add(card);

        // Scrim click closes
        setOnMouseClicked(e -> {
            if (e.getTarget() == this) {
                hide();
            }
        });
    }

    public void show() {
        setVisible(true);
        setManaged(true);
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }
}
