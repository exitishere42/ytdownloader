package de.exit.ytdownloader.ui;

import de.exit.ytdownloader.core.QueueManager;
import de.exit.ytdownloader.model.DownloadStatus;
import de.exit.ytdownloader.model.DownloadTask;
import de.exit.ytdownloader.util.LucideIcon;
import de.exit.ytdownloader.util.OSUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TaskCard extends HBox {

    private final DownloadTask task;
    private final QueueManager queueManager;

    private final ImageView thumbnailView;
    private final Label durationPill;
    private final Label titleLabel;
    private final Label channelLabel;
    private final Label statusLabel;
    private final Label speedEtaLabel;
    private final ProgressBar progressBar;
    private final Button openFolderButton;
    private final Button cancelButton;
    private final Button removeButton;

    public TaskCard(DownloadTask task, QueueManager queueManager) {
        this.task = task;
        this.queueManager = queueManager;

        getStyleClass().add("task-card");

        // 1. Thumbnail Container (110x62)
        StackPane thumbPane = new StackPane();
        thumbPane.setPrefSize(110, 62);
        thumbPane.setMinSize(110, 62);
        thumbPane.setMaxSize(110, 62);
        thumbPane.setStyle("-fx-background-color: #0A0A0A; -fx-background-radius: 4px; -fx-border-color: #2E2E2E; -fx-border-width: 1px; -fx-border-radius: 4px;");

        thumbnailView = new ImageView();
        thumbnailView.setFitWidth(110);
        thumbnailView.setFitHeight(62);
        thumbnailView.setPreserveRatio(true);

        durationPill = new Label();
        durationPill.getStyleClass().add("duration-pill");
        durationPill.setVisible(false);
        StackPane.setAlignment(durationPill, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(durationPill, new Insets(0, 4, 4, 0));

        thumbPane.getChildren().addAll(thumbnailView, durationPill);

        if (task.getThumbnailUrl() != null && !task.getThumbnailUrl().isBlank()) {
            loadThumbnailAsync(task.getThumbnailUrl());
        }

        // 2. Info Center
        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-text-fill: #F3F4F6; -fx-font-weight: bold; -fx-font-size: 13px;");
        titleLabel.setMaxWidth(480);

        Label formatBadge = new Label(task.getFormat().getDisplayName());
        formatBadge.getStyleClass().addAll("format-badge", "accent");

        titleRow.getChildren().addAll(titleLabel, formatBadge);

        // Channel & Status Row
        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        channelLabel = new Label(task.getChannel());
        channelLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        statusLabel = new Label(task.getStatus().getDisplayName());
        statusLabel.setStyle("-fx-text-fill: " + getStatusColorHex(task.getStatus()) + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        metaRow.getChildren().addAll(channelLabel, statusLabel);

        // Progress Bar
        progressBar = new ProgressBar(task.getProgress() / 100.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        // Telemetry Row
        HBox telemetryRow = new HBox(12);
        telemetryRow.setAlignment(Pos.CENTER_LEFT);

        speedEtaLabel = new Label();
        speedEtaLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11px;");

        telemetryRow.getChildren().add(speedEtaLabel);

        infoBox.getChildren().addAll(titleRow, metaRow, progressBar, telemetryRow);

        // 3. Right Action Buttons
        HBox actionBox = new HBox(6);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        openFolderButton = new Button();
        openFolderButton.setGraphic(LucideIcon.createIcon(LucideIcon.FOLDER, Theme.COLOR_ICON_TEAL, 0.8));
        openFolderButton.setTooltip(new Tooltip("Im Ordner anzeigen"));
        openFolderButton.getStyleClass().add("btn-icon-action");
        openFolderButton.setOnAction(e -> {
            if (task.getOutputPath() != null) {
                OSUtil.openInFileManager(task.getOutputPath());
            }
        });

        cancelButton = new Button();
        cancelButton.setGraphic(LucideIcon.createIcon(LucideIcon.CANCEL, Theme.COLOR_ICON_RED, 0.8));
        cancelButton.setTooltip(new Tooltip("Download abbrechen"));
        cancelButton.getStyleClass().addAll("btn-icon-action", "danger");
        cancelButton.setOnAction(e -> queueManager.cancelTask(task));

        removeButton = new Button();
        removeButton.setGraphic(LucideIcon.createIcon(LucideIcon.TRASH, Theme.COLOR_TEXT_MED, 0.8));
        removeButton.setTooltip(new Tooltip("Aus Liste entfernen"));
        removeButton.getStyleClass().add("btn-icon-action");
        removeButton.setOnAction(e -> queueManager.removeTask(task));

        actionBox.getChildren().addAll(openFolderButton, cancelButton, removeButton);

        getChildren().addAll(thumbPane, infoBox, actionBox);

        task.setChangeListener(t -> Platform.runLater(this::updateView));
        updateView();
    }

    private void updateView() {
        titleLabel.setText(task.getTitle());
        channelLabel.setText(task.getChannel());
        progressBar.setProgress(task.getProgress() / 100.0);

        statusLabel.setText(task.getStatus().getDisplayName());
        statusLabel.setStyle("-fx-text-fill: " + getStatusColorHex(task.getStatus()) + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        if (task.getDurationSeconds() > 0) {
            long m = (task.getDurationSeconds() % 3600) / 60;
            long s = task.getDurationSeconds() % 60;
            long h = task.getDurationSeconds() / 3600;
            durationPill.setText(h > 0 ? String.format("%d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s));
            durationPill.setVisible(true);
        }

        if (task.getStatus() == DownloadStatus.DOWNLOADING) {
            String stats = "";
            if (task.getSpeed() != null && !task.getSpeed().isBlank()) {
                stats += task.getSpeed();
            }
            if (task.getEta() != null && !task.getEta().isBlank()) {
                stats += (stats.isEmpty() ? "" : "  •  ") + "ETA " + task.getEta();
            }
            stats += String.format("  •  %.1f%%", task.getProgress());
            speedEtaLabel.setText(stats);
            speedEtaLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
        } else if (task.getStatus() == DownloadStatus.FAILED) {
            speedEtaLabel.setText(task.getErrorMessage() != null ? task.getErrorMessage() : "Download fehlgeschlagen");
            speedEtaLabel.setStyle("-fx-text-fill: " + Theme.HEX_ICON_RED + "; -fx-font-size: 11px;");
        } else if (task.getStatus() == DownloadStatus.COMPLETED) {
            speedEtaLabel.setText("Abgeschlossen");
            speedEtaLabel.setStyle("-fx-text-fill: " + Theme.HEX_ICON_GREEN + "; -fx-font-size: 11px;");
        } else {
            speedEtaLabel.setText("");
        }

        openFolderButton.setVisible(task.getStatus() == DownloadStatus.COMPLETED);
        openFolderButton.setManaged(task.getStatus() == DownloadStatus.COMPLETED);

        cancelButton.setVisible(task.getStatus().isActive());
        cancelButton.setManaged(task.getStatus().isActive());

        removeButton.setVisible(task.getStatus().isTerminal());
        removeButton.setManaged(task.getStatus().isTerminal());

        if (thumbnailView.getImage() == null && task.getThumbnailUrl() != null && !task.getThumbnailUrl().isBlank()) {
            loadThumbnailAsync(task.getThumbnailUrl());
        }
    }

    private void loadThumbnailAsync(String url) {
        try {
            Image img = new Image(url, 110, 62, true, true, true);
            img.progressProperty().addListener((obs, oldV, newV) -> {
                if (newV.doubleValue() >= 1.0 && !img.isError()) {
                    Platform.runLater(() -> thumbnailView.setImage(img));
                }
            });
        } catch (Exception ignored) {
        }
    }

    private String getStatusColorHex(DownloadStatus status) {
        return switch (status) {
            case COMPLETED -> Theme.HEX_ICON_GREEN;
            case DOWNLOADING, CONVERTING -> Theme.HEX_ICON_CYAN;
            case FAILED -> Theme.HEX_ICON_RED;
            case CANCELLED -> Theme.HEX_TEXT_LOW;
            case FETCHING -> Theme.HEX_ICON_AMBER;
            default -> Theme.HEX_TEXT_MED;
        };
    }
}
