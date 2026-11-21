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
import javafx.scene.paint.Color;

public class TaskCard extends HBox {

    private final DownloadTask task;
    private final QueueManager queueManager;

    private final ImageView thumbnailView;
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

        setSpacing(16);
        setPadding(new Insets(12, 16, 12, 16));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("surface-card");
        setStyle("-fx-background-color: " + Theme.SURFACE_CARD + "; -fx-background-radius: 4px; " +
                "-fx-border-color: " + Theme.BORDER + "; -fx-border-width: 1px; -fx-border-radius: 4px;");

        // 1. Thumbnail Container
        StackPane thumbPane = new StackPane();
        thumbPane.setPrefSize(120, 68);
        thumbPane.setMinSize(120, 68);
        thumbPane.setMaxSize(120, 68);
        thumbPane.setStyle("-fx-background-color: #0F0F0F; -fx-background-radius: 4px;");

        thumbnailView = new ImageView();
        thumbnailView.setFitWidth(120);
        thumbnailView.setFitHeight(68);
        thumbnailView.setPreserveRatio(true);
        thumbPane.getChildren().add(thumbnailView);

        if (task.getThumbnailUrl() != null && !task.getThumbnailUrl().isBlank()) {
            loadThumbnailAsync(task.getThumbnailUrl());
        }

        // 2. Info & Progress Center
        VBox infoBox = new VBox(6);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        titleLabel.setMaxWidth(500);

        Label formatBadge = new Label(task.getFormat().getDisplayName());
        formatBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_CONTAINER + "; -fx-text-fill: " +
                Theme.PRIMARY_CONTAINER_TEXT + "; -fx-background-radius: 4px; -fx-padding: 2px 8px; -fx-font-size: 11px; -fx-font-weight: bold;");

        titleRow.getChildren().addAll(titleLabel, formatBadge);

        channelLabel = new Label(task.getChannel());
        channelLabel.setStyle("-fx-text-fill: " + Theme.TEXT_SECONDARY + "; -fx-font-size: 12px;");

        // Progress Bar
        progressBar = new ProgressBar(task.getProgress() / 100.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(6);

        // Status & Stats Row
        HBox statsRow = new HBox(12);
        statusLabel = new Label(task.getStatus().getDisplayName());
        statusLabel.setStyle("-fx-text-fill: " + getStatusColor(task.getStatus()) + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        speedEtaLabel = new Label();
        speedEtaLabel.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 12px;");

        statsRow.getChildren().addAll(statusLabel, speedEtaLabel);
        infoBox.getChildren().addAll(titleRow, channelLabel, progressBar, statsRow);

        // 3. Actions Right
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        openFolderButton = new Button();
        openFolderButton.setGraphic(LucideIcon.createIcon(LucideIcon.FOLDER, Color.web(Theme.TEXT_PRIMARY), 0.8));
        openFolderButton.setTooltip(new Tooltip("Im Ordner anzeigen"));
        openFolderButton.getStyleClass().add("button-secondary");
        openFolderButton.setOnAction(e -> {
            if (task.getOutputPath() != null) {
                OSUtil.openInFileManager(task.getOutputPath());
            }
        });

        cancelButton = new Button();
        cancelButton.setGraphic(LucideIcon.createIcon(LucideIcon.CANCEL, Color.web(Theme.ERROR), 0.8));
        cancelButton.setTooltip(new Tooltip("Abbrechen"));
        cancelButton.getStyleClass().add("button-secondary");
        cancelButton.setOnAction(e -> queueManager.cancelTask(task));

        removeButton = new Button();
        removeButton.setGraphic(LucideIcon.createIcon(LucideIcon.TRASH, Color.web(Theme.TEXT_MUTED), 0.8));
        removeButton.setTooltip(new Tooltip("Aus Liste entfernen"));
        removeButton.getStyleClass().add("button-secondary");
        removeButton.setOnAction(e -> queueManager.removeTask(task));

        actionBox.getChildren().addAll(openFolderButton, cancelButton, removeButton);

        getChildren().addAll(thumbPane, infoBox, actionBox);

        // Task-Listener registrieren
        task.setChangeListener(t -> Platform.runLater(this::updateView));
        updateView();
    }

    private void updateView() {
        titleLabel.setText(task.getTitle());
        channelLabel.setText(task.getChannel());
        progressBar.setProgress(task.getProgress() / 100.0);

        statusLabel.setText(task.getStatus().getDisplayName());
        statusLabel.setStyle("-fx-text-fill: " + getStatusColor(task.getStatus()) + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        if (task.getStatus() == DownloadStatus.DOWNLOADING) {
            String stats = "";
            if (task.getSpeed() != null && !task.getSpeed().isBlank()) {
                stats += task.getSpeed();
            }
            if (task.getEta() != null && !task.getEta().isBlank()) {
                stats += (stats.isEmpty() ? "" : " • ") + "ETA: " + task.getEta();
            }
            stats += String.format(" (%.1f%%)", task.getProgress());
            speedEtaLabel.setText(stats);
        } else if (task.getStatus() == DownloadStatus.FAILED) {
            speedEtaLabel.setText(task.getErrorMessage() != null ? task.getErrorMessage() : "");
            speedEtaLabel.setStyle("-fx-text-fill: " + Theme.ERROR + "; -fx-font-size: 12px;");
        } else {
            speedEtaLabel.setText("");
        }

        // Button States
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
            Image img = new Image(url, 120, 68, true, true, true);
            img.progressProperty().addListener((obs, oldV, newV) -> {
                if (newV.doubleValue() >= 1.0 && !img.isError()) {
                    Platform.runLater(() -> thumbnailView.setImage(img));
                }
            });
        } catch (Exception ignored) {
        }
    }

    private String getStatusColor(DownloadStatus status) {
        return switch (status) {
            case COMPLETED -> Theme.SUCCESS;
            case DOWNLOADING, CONVERTING -> Theme.PRIMARY;
            case FAILED -> Theme.ERROR;
            case CANCELLED -> Theme.TEXT_MUTED;
            default -> Theme.TEXT_SECONDARY;
        };
    }
}
