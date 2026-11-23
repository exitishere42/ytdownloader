package de.exit.ytdownloader.ui;

import de.exit.ytdownloader.core.DependencyManager;
import de.exit.ytdownloader.core.DownloadEngine;
import de.exit.ytdownloader.core.QueueManager;
import de.exit.ytdownloader.model.DownloadFormat;
import de.exit.ytdownloader.model.DownloadStatus;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MainWindow extends StackPane {

    private final Stage stage;
    private final DependencyManager dependencyManager;
    private final DownloadEngine engine;
    private final QueueManager queueManager;
    private final SettingsService settingsService;
    private final MetadataService metadataService;

    // UI Controls
    private TextField urlField;
    private ComboBox<DownloadFormat> formatCombo;
    private Button downloadButton;
    private VBox queueContainer;
    private VBox emptyStateBox;
    private Label queueCountLabel;
    private Label trafficStatusText;
    private Circle trafficDot;
    private Label folderLabel;

    // Preview Dock
    private HBox previewDock;
    private ImageView previewThumbnail;
    private Label previewTitle;
    private Label previewChannel;
    private Label previewDuration;
    private VideoMetadata currentMetadata;

    // In-App Modal Overlays
    private SettingsOverlay settingsOverlay;
    private AboutUpdateDialog updateDialog;
    private BrandActionMenu brandActionMenu;

    private final Map<String, TaskCard> cardMap = new HashMap<>();

    public MainWindow(Stage stage) {
        this.stage = stage;
        this.dependencyManager = new DependencyManager();
        this.engine = new DownloadEngine(dependencyManager);
        this.settingsService = new SettingsService();
        this.metadataService = new MetadataService(dependencyManager);
        this.queueManager = new QueueManager(engine, settingsService, metadataService);

        this.brandActionMenu = new BrandActionMenu();
        this.updateDialog = new AboutUpdateDialog();
        this.brandActionMenu.setAction("updates", () -> updateDialog.show());

        setStyle("-fx-background-color: #121212;");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(buildHeaderBar());
        mainLayout.setCenter(buildCenterContent());

        settingsOverlay = new SettingsOverlay(settingsService, dependencyManager, this::updateFolderPill);

        getChildren().addAll(mainLayout, settingsOverlay, updateDialog);

        checkAndInitDependencies();
    }

    private HBox buildHeaderBar() {
        HBox header = new HBox(12);
        header.getStyleClass().add("header-bar");

        // 1. Brand Button (Logo + App Title)
        Button brandBtn = new Button("ytdownloader");
        brandBtn.getStyleClass().add("brand-button");
        brandBtn.setGraphic(LucideIcon.createIcon(LucideIcon.DOWNLOAD, Theme.COLOR_PRIMARY_RED, 1.0));
        brandBtn.setOnAction(e -> {
            if (brandActionMenu.isShowing()) {
                brandActionMenu.hide();
            } else {
                brandActionMenu.showBelow(brandBtn);
            }
        });

        // Separator
        Line sep1 = new Line(0, 0, 0, 22);
        sep1.getStyleClass().add("header-separator");

        // 2. Tab Navigation Group
        HBox tabGroup = new HBox(4);
        tabGroup.getStyleClass().add("tab-button-group");

        Button navDownloads = new Button("Downloads");
        navDownloads.getStyleClass().addAll("nav-tab-button", "active");
        navDownloads.setGraphic(LucideIcon.createIcon(LucideIcon.DOWNLOAD, Theme.COLOR_ICON_CYAN, 0.8));

        Button navSettings = new Button("Einstellungen");
        navSettings.getStyleClass().add("nav-tab-button");
        navSettings.setGraphic(LucideIcon.createIcon(LucideIcon.SETTINGS, Theme.COLOR_ICON_AMBER, 0.8));
        navSettings.setOnAction(e -> settingsOverlay.show());

        tabGroup.getChildren().addAll(navDownloads, navSettings);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 3. Right Status Actions
        // Folder Pill
        Button folderBtn = new Button();
        folderBtn.getStyleClass().add("custom-dropdown-btn");
        folderBtn.setTooltip(new Tooltip("Download-Ordner öffnen"));

        HBox folderContent = new HBox(6);
        folderContent.setAlignment(Pos.CENTER_LEFT);
        folderLabel = new Label(getShortenedDownloadDir());
        folderLabel.getStyleClass().add("custom-dropdown-label");
        folderContent.getChildren().addAll(LucideIcon.createIcon(LucideIcon.FOLDER, Theme.COLOR_ICON_TEAL, 0.8), folderLabel);
        folderBtn.setGraphic(folderContent);
        folderBtn.setOnAction(e -> OSUtil.openInFileManager(Paths.get(settingsService.getSettings().getDownloadDirectory())));

        // Traffic Pill (JDMX Style)
        HBox trafficPill = new HBox(6);
        trafficPill.getStyleClass().add("traffic-pill");

        trafficDot = new Circle(4);
        trafficDot.setFill(Theme.COLOR_ICON_GREEN);

        trafficStatusText = new Label("Bereit");
        trafficStatusText.setStyle("-fx-text-fill: " + Theme.HEX_ICON_GREEN + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        trafficPill.getChildren().addAll(trafficDot, trafficStatusText);

        header.getChildren().addAll(brandBtn, sep1, tabGroup, spacer, folderBtn, trafficPill);
        return header;
    }

    private VBox buildCenterContent() {
        VBox center = new VBox(14);
        center.setPadding(new Insets(16, 20, 16, 20));

        // 1. Hero Input Dock
        VBox heroDock = new VBox(10);
        heroDock.getStyleClass().add("hero-input-dock");

        // Search/URL Input Box
        HBox searchBox = new HBox(8);
        searchBox.getStyleClass().add("search-box");
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        urlField = new TextField();
        urlField.setPromptText("YouTube-Link hier einfügen (z. B. https://www.youtube.com/watch?v=...)");
        urlField.getStyleClass().add("search-field");
        HBox.setHgrow(urlField, Priority.ALWAYS);
        urlField.textProperty().addListener((obs, oldVal, newVal) -> handleUrlChanged(newVal));

        Button pasteBtn = new Button();
        pasteBtn.setGraphic(LucideIcon.createIcon(LucideIcon.CLIPBOARD, Theme.COLOR_TEXT_MED, 0.8));
        pasteBtn.setTooltip(new Tooltip("Aus Zwischenablage einfügen"));
        pasteBtn.getStyleClass().add("btn-icon-action");
        pasteBtn.setOnAction(e -> {
            String clip = Clipboard.getSystemClipboard().getString();
            if (clip != null && !clip.isBlank()) {
                urlField.setText(clip.trim());
            }
        });

        searchBox.getChildren().addAll(LucideIcon.createIcon(LucideIcon.LINK, Theme.COLOR_TEXT_MED, 0.8), urlField, pasteBtn);

        // Action & Format Row
        HBox actionRow = new HBox(12);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll(DownloadFormat.values());
        formatCombo.setValue(settingsService.getSettings().getDefaultFormat());

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        downloadButton = new Button("Herunterladen");
        downloadButton.setGraphic(LucideIcon.createIcon(LucideIcon.DOWNLOAD, javafx.scene.paint.Color.WHITE, 0.9));
        downloadButton.getStyleClass().add("btn-primary-action");
        downloadButton.setOnAction(e -> onDownloadClicked());

        actionRow.getChildren().addAll(formatCombo, spacer, downloadButton);

        // Preview Dock
        buildPreviewDock();

        heroDock.getChildren().addAll(searchBox, actionRow, previewDock);

        // 2. Queue Section
        VBox queueSection = new VBox(8);
        VBox.setVgrow(queueSection, Priority.ALWAYS);

        // Section Title Header
        HBox queueHeader = new HBox(8);
        queueHeader.setAlignment(Pos.CENTER_LEFT);
        queueHeader.setPadding(new Insets(6, 2, 6, 2));

        Label sectionLabel = new Label("WARTESCHLANGE");
        sectionLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

        queueCountLabel = new Label("0");
        queueCountLabel.getStyleClass().add("format-badge");

        HBox spacer2 = new HBox();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button clearBtn = new Button("Erledigte leeren");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setStyle("-fx-padding: 3 8; -fx-font-size: 11px;");
        clearBtn.setOnAction(e -> {
            queueManager.getTasks().removeIf(t -> t.getStatus().isTerminal());
        });

        queueHeader.getChildren().addAll(sectionLabel, queueCountLabel, spacer2, clearBtn);

        // Queue Items Container
        queueContainer = new VBox(8);

        // Empty State (JDMX Style)
        emptyStateBox = new VBox(10);
        emptyStateBox.setAlignment(Pos.CENTER);
        emptyStateBox.setPadding(new Insets(48, 0, 48, 0));

        Label emptyIcon = new Label();
        emptyIcon.setGraphic(LucideIcon.createIcon(LucideIcon.DOWNLOAD, javafx.scene.paint.Color.web("#282828"), 2.4));

        Label emptyTitle = new Label("Keine aktiven Downloads");
        emptyTitle.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label emptySub = new Label("Füge oben einen YouTube-Link ein, um den Download zu starten.");
        emptySub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        emptyStateBox.getChildren().addAll(emptyIcon, emptyTitle, emptySub);
        queueContainer.getChildren().add(emptyStateBox);

        ScrollPane scrollPane = new ScrollPane(queueContainer);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        queueSection.getChildren().addAll(queueHeader, scrollPane);

        center.getChildren().addAll(heroDock, queueSection);

        // Listen for Queue changes
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
            boolean empty = queueManager.getTasks().isEmpty();
            emptyStateBox.setVisible(empty);
            emptyStateBox.setManaged(empty);
            queueCountLabel.setText(String.valueOf(queueManager.getTasks().size()));
            updateTelemetry();
        });

        return center;
    }

    private void buildPreviewDock() {
        previewDock = new HBox(14);
        previewDock.getStyleClass().add("preview-card");
        previewDock.setAlignment(Pos.CENTER_LEFT);
        previewDock.setVisible(false);
        previewDock.setManaged(false);

        // Thumbnail with Duration
        StackPane thumbPane = new StackPane();
        thumbPane.setPrefSize(110, 62);
        thumbPane.setMinSize(110, 62);
        thumbPane.setMaxSize(110, 62);
        thumbPane.setStyle("-fx-background-color: #0A0A0A; -fx-background-radius: 4px; -fx-border-color: #2E2E2E; -fx-border-width: 1px; -fx-border-radius: 4px;");

        previewThumbnail = new ImageView();
        previewThumbnail.setFitWidth(110);
        previewThumbnail.setFitHeight(62);
        previewThumbnail.setPreserveRatio(true);

        previewDuration = new Label();
        previewDuration.getStyleClass().add("duration-pill");
        StackPane.setAlignment(previewDuration, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(previewDuration, new Insets(0, 4, 4, 0));

        thumbPane.getChildren().addAll(previewThumbnail, previewDuration);

        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        previewTitle = new Label();
        previewTitle.setStyle("-fx-text-fill: #F3F4F6; -fx-font-weight: bold; -fx-font-size: 13px;");

        previewChannel = new Label();
        previewChannel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");

        infoBox.getChildren().addAll(previewTitle, previewChannel);
        previewDock.getChildren().addAll(thumbPane, infoBox);
    }

    private void handleUrlChanged(String url) {
        if (url == null || url.isBlank() || !url.contains("youtu")) {
            previewDock.setVisible(false);
            previewDock.setManaged(false);
            currentMetadata = null;
            return;
        }

        metadataService.fetchMetadataAsync(url.trim()).thenAccept(meta -> Platform.runLater(() -> {
            this.currentMetadata = meta;
            previewTitle.setText(meta.getTitle());
            previewChannel.setText(meta.getChannel());
            previewDuration.setText(meta.getFormattedDuration());

            if (meta.getThumbnailUrl() != null && !meta.getThumbnailUrl().isBlank()) {
                Image img = new Image(meta.getThumbnailUrl(), 110, 62, true, true, true);
                previewThumbnail.setImage(img);
            }

            previewDock.setVisible(true);
            previewDock.setManaged(true);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                previewDock.setVisible(false);
                previewDock.setManaged(false);
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

        urlField.clear();
        previewDock.setVisible(false);
        previewDock.setManaged(false);
        currentMetadata = null;
    }

    private void updateTelemetry() {
        long running = queueManager.getTasks().stream().filter(t -> t.getStatus().isActive()).count();
        if (running > 0) {
            trafficDot.setFill(Theme.COLOR_ICON_AMBER);
            trafficStatusText.setText(running + " aktiv");
            trafficStatusText.setStyle("-fx-text-fill: " + Theme.HEX_ICON_AMBER + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            trafficDot.setFill(Theme.COLOR_ICON_GREEN);
            trafficStatusText.setText("Bereit");
            trafficStatusText.setStyle("-fx-text-fill: " + Theme.HEX_ICON_GREEN + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
    }

    private void updateFolderPill() {
        folderLabel.setText(getShortenedDownloadDir());
    }

    private String getShortenedDownloadDir() {
        Path p = Paths.get(settingsService.getSettings().getDownloadDirectory());
        return p.getFileName() != null ? p.getFileName().toString() : p.toString();
    }

    private void checkAndInitDependencies() {
        if (!dependencyManager.isYtDlpInstalled()) {
            trafficDot.setFill(Theme.COLOR_ICON_AMBER);
            trafficStatusText.setText("Lädt yt-dlp...");
            trafficStatusText.setStyle("-fx-text-fill: " + Theme.HEX_ICON_AMBER + "; -fx-font-size: 11px; -fx-font-weight: bold;");

            dependencyManager.ensureYtDlpAsync(null).thenAccept(path -> Platform.runLater(this::updateTelemetry));
        }
    }

    public void cleanup() {
        queueManager.shutdown();
    }
}
