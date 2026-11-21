package de.exit.ytdownloader.ui;

import de.exit.ytdownloader.model.AppSettings;
import de.exit.ytdownloader.model.DownloadFormat;
import de.exit.ytdownloader.service.SettingsService;
import de.exit.ytdownloader.util.LucideIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class SettingsDialog {

    public static void show(Stage owner, SettingsService settingsService, Runnable onSaveCallback) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Einstellungen");

        AppSettings settings = settingsService.getSettings();

        VBox root = new VBox(20);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: " + Theme.BG_BASE + ";");

        Label heading = new Label("Einstellungen");
        heading.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        // 1. Download-Verzeichnis
        VBox dirBox = new VBox(8);
        Label dirLabel = new Label("Download-Verzeichnis");
        dirLabel.setStyle("-fx-text-fill: " + Theme.TEXT_SECONDARY + "; -fx-font-size: 12px;");

        HBox dirInputRow = new HBox(8);
        TextField dirField = new TextField(settings.getDownloadDirectory());
        dirField.setEditable(false);
        HBox.setHgrow(dirField, Priority.ALWAYS);

        Button browseBtn = new Button();
        browseBtn.setGraphic(LucideIcon.createIcon(LucideIcon.FOLDER, Color.web(Theme.TEXT_PRIMARY), 0.8));
        browseBtn.getStyleClass().add("button-secondary");
        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Download-Ordner wählen");
            File initial = new File(settings.getDownloadDirectory());
            if (initial.exists()) {
                chooser.setInitialDirectory(initial);
            }
            File selected = chooser.showDialog(dialog);
            if (selected != null) {
                dirField.setText(selected.getAbsolutePath());
            }
        });

        dirInputRow.getChildren().addAll(dirField, browseBtn);
        dirBox.getChildren().addAll(dirLabel, dirInputRow);

        // 2. Standard-Format
        VBox formatBox = new VBox(8);
        Label formatLabel = new Label("Standard-Format");
        formatLabel.setStyle("-fx-text-fill: " + Theme.TEXT_SECONDARY + "; -fx-font-size: 12px;");

        ComboBox<DownloadFormat> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll(DownloadFormat.values());
        formatCombo.setValue(settings.getDefaultFormat());
        formatCombo.setMaxWidth(Double.MAX_VALUE);
        formatBox.getChildren().addAll(formatLabel, formatCombo);

        // 3. Parallele Downloads
        VBox threadBox = new VBox(8);
        Label threadLabel = new Label("Gleichzeitige Downloads");
        threadLabel.setStyle("-fx-text-fill: " + Theme.TEXT_SECONDARY + "; -fx-font-size: 12px;");

        Spinner<Integer> threadSpinner = new Spinner<>(1, 5, settings.getMaxConcurrentDownloads());
        threadSpinner.setMaxWidth(Double.MAX_VALUE);
        threadBox.getChildren().addAll(threadLabel, threadSpinner);

        // Buttons
        HBox buttonBar = new HBox(12);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Abbrechen");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Speichern");
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setOnAction(e -> {
            settings.setDownloadDirectory(dirField.getText());
            settings.setDefaultFormat(formatCombo.getValue());
            settings.setMaxConcurrentDownloads(threadSpinner.getValue());
            settingsService.saveSettings();
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dialog.close();
        });

        buttonBar.getChildren().addAll(cancelBtn, saveBtn);

        root.getChildren().addAll(heading, dirBox, formatBox, threadBox, buttonBar);

        Scene scene = new Scene(root, 480, 360);
        scene.getStylesheets().add(SettingsDialog.class.getResource("/de/exit/ytdownloader/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
