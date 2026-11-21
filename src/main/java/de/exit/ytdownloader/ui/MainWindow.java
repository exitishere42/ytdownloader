package de.exit.ytdownloader.ui;

import de.exit.ytdownloader.core.DependencyManager;
import de.exit.ytdownloader.core.DownloadEngine;
import de.exit.ytdownloader.core.QueueManager;
import de.exit.ytdownloader.model.DownloadFormat;
import de.exit.ytdownloader.model.DownloadTask;
import de.exit.ytdownloader.model.VideoMetadata;
import de.exit.ytdownloader.service.MetadataService;
import de.exit.ytdownloader.service.SettingsService;
import de.exit.ytdownloader.util.LucideIcon;
import de.exit.ytdownloader.util.OSUtil;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MainWindow extends BorderPane {

    private final Stage stage;
    private final DependencyManager dependencyManager;
    private final DownloadEngine engine;
    private final QueueManager queueManager;
    private final SettingsService settingsService;
    private final MetadataService metadataService;

    private TextField urlField;
    private ComboBox<DownloadFormat> formatCombo;
    private Button downloadButton;
    private VBox queueContainer;
    private Label emptyQueueLabel;
    private Label footerStatusLabel;
    private Label footerDirLabel;

    // Dependency Banner
    private HBox dependencyBanner;
    private Label dependencyStatusLabel;
    private ProgressBar dependencyProgressBar;

    // Preview Card
    private HBox previewCard;
    private ImageView previewThumbnail;
    private Label previewTitle;
    private Label previewChannel;
    private Label previewDuration;
    private VideoMetadata currentMetadata;

    private final Map<String, TaskCard> cardMap = new HashMap<>();

    public MainWindow(Stage stage) {
        this.stage = stage;
        this.dependencyManager = new DependencyManager();
        this.engine = new DownloadEngine(dependencyManager);
        this.settingsService = new SettingsService();
        this.queueManager = new QueueManager(engine, settingsService);
        this.metadataService = new MetadataService(dependencyManager);

        setStyle("-fx-background-color: " + Theme.BG_BASE + ";");

        buildHeader();
        buildCenter();
        buildFooter();

        checkAndInitDependencies();
    }

    private void buildHeader() {
        VBox headerBox = new VBox(12);
        headerBox.setPadding(new Insets(16, 20, 16, 20));
        headerBox.setStyle("-fx-background-color: " + Theme.SURFACE_CARD + "; -fx-border-color: " + Theme.BORDER + "; -fx-border-width: 0 0 1px 0;");

        // Top Row: Title + Settings
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label logoIcon = new Label();
        logoIcon.setGraphic(LucideIcon.createIcon(LucideIcon.PLAY, Color.web(Theme.PRIMARY), 1.2));

        Label appTitle = new Label("YouTube Downloader");
        appTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button settingsButton = new Button();
        settingsButton.setGraphic(LucideIcon.createIcon(LucideIcon.SETTINGS, Color.web(Theme.TEXT_PRIMARY), 0.9));
        settingsButton.getStyleClass().add("button-secondary");
        settingsButton.setTooltip(new Tooltip("Einstellungen"));
        settingsButton.setOnAction(e -> SettingsDialog.show(stage, settingsService, this::updateFooter));

        topRow.getChildren().addAll(logoIcon, appTitle, spacer, settingsButton);

        // Dependency Banner (hidden by default)
        dependencyBanner = new HBox(12);
        dependencyBanner.setAlignment(Pos.CENTER_LEFT);
        dependencyBanner.setPadding(new Insets(8, 12, 8, 12));
        dependencyBanner.setStyle("-fx-background-color: " + Theme.SURFACE_ELEVATED + "; -fx-background-radius: 4px;");
        dependencyBanner.setVisible(false);
        dependencyBanner.setManaged(false);

        dependencyStatusLabel = new Label("Initialisiere yt-dlp...");
        dependencyStatusLabel.setStyle("-fx-text-fill: " + Theme.TEXT_SECONDARY + "; -fx-font-size: 12px;");

        dependencyProgressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        dependencyProgressBar.setPrefWidth(140);
        dependencyProgressBar.setPrefHeight(6);

        dependencyBanner.getChildren().addAll(dependencyStatusLabel, dependencyProgressBar);

        // URL Input Row
        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        urlField = new TextField();
        urlField.setPromptText("YouTube-Video-URL einfügen (z. B. https://www.youtube.com/watch?v=...)");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        // Listener für automatischen Metadaten-Fetch
        urlField.textProperty().addListener((obs, oldVal, newVal) -> handleUrlChanged(newVal));

        Button pasteBtn = new Button();
        pasteBtn.setGraphic(LucideIcon.createIcon(LucideIcon.CLIPBOARD, Color.web(Theme.TEXT_PRIMARY), 0.8));
        pasteBtn.setTooltip(new Tooltip("Aus Zwischenablage einfügen"));
        pasteBtn.getStyleClass().add("button-secondary");
        pasteBtn.setOnAction(e -> {
            String clip = Clipboard.getSystemClipboard().getString();
            if (clip != null && !clip.isBlank()) {
                urlField.setText(clip.trim());
            }
        });

        formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll(DownloadFormat.values());
        formatCombo.setValue(settingsService.getSettings().getDefaultFormat());

        downloadButton = new Button("Herunterladen");
        downloadButton.setGraphic(LucideIcon.createIcon(LucideIcon.DOWNLOAD, Color.WHITE, 0.9));
        downloadButton.getStyleClass().add("button-primary");
        downloadButton.setOnAction(e -> onDownloadClicked());

        inputRow.getChildren().addAll(urlField, pasteBtn, formatCombo, downloadButton);

        // Preview Card
        buildPreviewCard();

        headerBox.getChildren().addAll(topRow, dependencyBanner, inputRow, previewCard);
        setTop(headerBox);
    }

    private void buildPreviewCard() {
        previewCard = new HBox(16);
        previewCard.setPadding(new Insets(10, 14, 10, 14));
        previewCard.setAlignment(Pos.CENTER_LEFT);
        previewCard.setStyle("-fx-background-color: " + Theme.SURFACE_ELEVATED + "; -fx-background-radius: 4px; " +
                "-fx-border-color: " + Theme.BORDER + "; -fx-border-width: 1px; -fx-border-radius: 4px;");
        previewCard.setVisible(false);
        previewCard.setManaged(false);

        StackPane thumbPane = new StackPane();
        thumbPane.setPrefSize(100, 56);
        thumbPane.setMinSize(100, 56);
        thumbPane.setMaxSize(100, 56);
        thumbPane.setStyle("-fx-background-color: #000000; -fx-background-radius: 4px;");

        previewThumbnail = new ImageView();
        previewThumbnail.setFitWidth(100);
        previewThumbnail.setFitHeight(56);
        previewThumbnail.setPreserveRatio(true);
        thumbPane.getChildren().add(previewThumbnail);

        VBox previewInfo = new VBox(4);
        HBox.setHgrow(previewInfo, Priority.ALWAYS);

        previewTitle = new Label();
        previewTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 13px;");

        HBox subRow = new HBox(12);
        previewChannel = new Label();
        previewChannel.setStyle("-fx-text-fill: " + Theme.TEXT_SECONDARY + "; -fx-font-size: 11px;");

        previewDuration = new Label();
        previewDuration.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 11px;");

        subRow.getChildren().addAll(previewChannel, previewDuration);
        previewInfo.getChildren().addAll(previewTitle, subRow);

        previewCard.getChildren().addAll(thumbPane, previewInfo);
    }

    private void buildCenter() {
        VBox centerBox = new VBox(12);
        centerBox.setPadding(new Insets(16, 20, 16, 20));

        Label sectionTitle = new Label("Download-Warteschlange");
        sectionTitle.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        queueContainer = new VBox(10);

        emptyQueueLabel = new Label("Keine aktiven Downloads. Füge oben eine URL ein.");
        emptyQueueLabel.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 13px; -fx-padding: 30px 0 0 0;");
        emptyQueueLabel.setAlignment(Pos.CENTER);
        emptyQueueLabel.setMaxWidth(Double.MAX_VALUE);

        queueContainer.getChildren().add(emptyQueueLabel);

        ScrollPane scrollPane = new ScrollPane(queueContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        centerBox.getChildren().addAll(sectionTitle, scrollPane);
        setCenter(centerBox);

        // Queue Listener
        queueManager.getTasks().addListener((ListChangeListener<DownloadTask>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (DownloadTask task : c.getAddedSubList()) {
                        TaskCard card = new TaskCard(task, queueManager);
                        cardMap.put(task.getId(), card);
                        queueContainer.getChildren().add(0, card);
                    }
                }
                if (c.wasRemoved()) {
                    for (DownloadTask task : c.getRemoved()) {
                        TaskCard card = cardMap.remove(task.getId());
                        if (card != null) {
                            queueContainer.getChildren().remove(card);
                        }
                    }
                }
            }
            emptyQueueLabel.setVisible(queueManager.getTasks().isEmpty());
            emptyQueueLabel.setManaged(queueManager.getTasks().isEmpty());
            updateFooter();
        });
    }

    private void buildFooter() {
        HBox footer = new HBox(16);
        footer.setPadding(new Insets(10, 20, 10, 20));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-background-color: " + Theme.SURFACE_CARD + "; -fx-border-color: " + Theme.BORDER + "; -fx-border-width: 1px 0 0 0;");

        footerStatusLabel = new Label("Bereit");
        footerStatusLabel.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 11px;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footerDirLabel = new Label("Ziel: " + settingsService.getSettings().getDownloadDirectory());
        footerDirLabel.setStyle("-fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 11px;");

        footer.getChildren().addAll(footerStatusLabel, spacer, footerDirLabel);
        setBottom(footer);
    }

    private void updateFooter() {
        int count = queueManager.getTasks().size();
        footerStatusLabel.setText(count == 0 ? "Bereit" : count + " Download" + (count > 1 ? "s" : "") + " in Liste");
        footerDirLabel.setText("Ziel: " + settingsService.getSettings().getDownloadDirectory());
    }

    private void checkAndInitDependencies() {
        if (!dependencyManager.isYtDlpInstalled()) {
            dependencyBanner.setVisible(true);
            dependencyBanner.setManaged(true);
            dependencyStatusLabel.setText("Lade yt-dlp automatisch herunter...");

            dependencyManager.ensureYtDlpAsync(pct -> Platform.runLater(() -> {
                dependencyProgressBar.setProgress(pct / 100.0);
                dependencyStatusLabel.setText(String.format("Lade yt-dlp herunter: %.0f%%", pct));
            })).thenAccept(path -> Platform.runLater(() -> {
                dependencyBanner.setVisible(false);
                dependencyBanner.setManaged(false);
                footerStatusLabel.setText("yt-dlp bereit");
            })).exceptionally(ex -> {
                Platform.runLater(() -> {
                    dependencyStatusLabel.setText("Fehler: " + ex.getMessage());
                    dependencyStatusLabel.setStyle("-fx-text-fill: " + Theme.ERROR + ";");
                });
                return null;
            });
        }
    }

    private void handleUrlChanged(String url) {
        if (url == null || url.isBlank() || !url.contains("youtu")) {
            previewCard.setVisible(false);
            previewCard.setManaged(false);
            currentMetadata = null;
            return;
        }

        // Asynchron Metadaten laden
        metadataService.fetchMetadataAsync(url.trim()).thenAccept(meta -> Platform.runLater(() -> {
            this.currentMetadata = meta;
            previewTitle.setText(meta.getTitle());
            previewChannel.setText(meta.getChannel());
            previewDuration.setText(meta.getFormattedDuration());

            if (meta.getThumbnailUrl() != null && !meta.getThumbnailUrl().isBlank()) {
                Image img = new Image(meta.getThumbnailUrl(), 100, 56, true, true, true);
                previewThumbnail.setImage(img);
            }

            previewCard.setVisible(true);
            previewCard.setManaged(true);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                previewCard.setVisible(false);
                previewCard.setManaged(false);
            });
            return null;
        });
    }

    private void onDownloadClicked() {
        String url = urlField.getText();
        if (url == null || url.isBlank()) {
            return;
        }

        DownloadFormat format = formatCombo.getValue() != null ? formatCombo.getValue() : DownloadFormat.MP4_BEST;
        DownloadTask task = new DownloadTask(url.trim(), format);

        if (currentMetadata != null) {
            task.setMetadata(currentMetadata);
        }

        queueManager.enqueue(task);

        // URL-Feld leeren
        urlField.clear();
        previewCard.setVisible(false);
        previewCard.setManaged(false);
        currentMetadata = null;
    }

    public void cleanup() {
        queueManager.shutdown();
    }
}
