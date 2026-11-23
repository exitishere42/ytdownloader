package de.exit.ytdownloader.ui;

import de.exit.ytdownloader.service.UpdateService;
import de.exit.ytdownloader.util.LucideIcon;
import de.exit.ytdownloader.util.OSUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.logging.Logger;

/**
 * In-App Modal Dialog für das Prüfen und Einspielen von Software-Updates (JDMX Studio Dark Style).
 */
public class AboutUpdateDialog extends StackPane {

    private static final Logger LOGGER = Logger.getLogger(AboutUpdateDialog.class.getName());

    private final VBox card;
    private Label lblTitle;
    private Label lblCurTitle;
    private Label lblLatTitle;
    private Label lblCurrentVersion;
    private Label lblLatestVersion;
    private Label lblStatus;
    private ProgressIndicator spinner;
    private ScrollPane changelogScrollPane;
    private Label changelogLabel;
    private ProgressBar progressBar;
    private Label lblProgressDetail;
    private HBox actionButtonBox;
    private Button btnClose;
    private Button btnCloseFooter;
    private Button btnUpdate;
    private Button btnRetry;
    private UpdateService.ReleaseInfo lastRelease;
    private boolean isChecking = true;
    private boolean isError = false;

    public AboutUpdateDialog() {
        getStyleClass().add("modal-overlay-scrim");
        setVisible(false);
        setManaged(false);

        this.card = new VBox(14);
        card.getStyleClass().add("settings-dialog-card");
        card.setMinWidth(480);
        card.setPrefWidth(520);
        card.setMaxWidth(560);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        buildUi();
        getChildren().add(card);
    }

    private void buildUi() {
        // 1. Header (Icon, Titel, Schließen-Button)
        HBox header = buildHeader();

        // 2. Versions-Kachel (Installiert vs. Neueste Version)
        HBox versionCard = buildVersionCard();

        // 3. Status & Update-Bereich
        VBox statusCard = buildStatusCard();

        // 4. Footer (GitHub Link & Schließen)
        HBox footer = buildFooter();

        card.getChildren().addAll(header, versionCard, statusCard, footer);
    }

    private HBox buildHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        var iconLogo = LucideIcon.createIcon(LucideIcon.ARROW_UP_CIRCLE, Color.web("#38BDF8"), 0.9);
        this.lblTitle = new Label("Updates");
        lblTitle.getStyleClass().add("dialog-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.btnClose = new Button();
        btnClose.getStyleClass().add("dialog-close-btn");
        btnClose.setGraphic(LucideIcon.createIcon(LucideIcon.CANCEL, Theme.COLOR_TEXT_MED, 0.8));
        btnClose.setOnAction(e -> hide());

        header.getChildren().addAll(iconLogo, lblTitle, spacer, btnClose);
        return header;
    }

    private HBox buildVersionCard() {
        HBox versionCard = new HBox(16);
        versionCard.setAlignment(Pos.CENTER_LEFT);
        versionCard.setPadding(new Insets(12, 16, 12, 16));
        versionCard.getStyleClass().add("update-status-pill");

        // Installierte Version
        VBox curBox = new VBox(2);
        this.lblCurTitle = new Label("INSTALLIERTE VERSION");
        lblCurTitle.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 10px; -fx-font-weight: bold;");
        this.lblCurrentVersion = new Label("v" + UpdateService.CURRENT_VERSION);
        lblCurrentVersion.setStyle("-fx-text-fill: #F3F4F6; -fx-font-size: 14px; -fx-font-weight: bold;");
        curBox.getChildren().addAll(lblCurTitle, lblCurrentVersion);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Neueste Version auf GitHub
        VBox latBox = new VBox(2);
        latBox.setAlignment(Pos.CENTER_RIGHT);
        this.lblLatTitle = new Label("NEUESTE VERSION");
        lblLatTitle.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 10px; -fx-font-weight: bold;");
        this.lblLatestVersion = new Label("Prüfe...");
        lblLatestVersion.setStyle("-fx-text-fill: #38BDF8; -fx-font-size: 14px; -fx-font-weight: bold;");
        latBox.getChildren().addAll(lblLatTitle, lblLatestVersion);

        versionCard.getChildren().addAll(curBox, spacer, latBox);
        return versionCard;
    }

    private VBox buildStatusCard() {
        VBox statusCard = new VBox(10);
        statusCard.setPadding(new Insets(12, 16, 12, 16));
        statusCard.getStyleClass().add("update-status-pill");

        // Status-Zeile mit Spinner
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        this.spinner = new ProgressIndicator();
        spinner.setMaxSize(16, 16);
        spinner.setMinSize(16, 16);
        spinner.setPrefSize(16, 16);
        spinner.setStyle("-fx-progress-color: #38BDF8;");

        this.lblStatus = new Label("Suche nach neuen Updates auf GitHub...");
        lblStatus.setStyle("-fx-text-fill: #F3F4F6; -fx-font-size: 12px;");
        statusRow.getChildren().addAll(spinner, lblStatus);

        // Changelog / Release Notes
        this.changelogLabel = new Label();
        changelogLabel.setWrapText(true);
        changelogLabel.setStyle("-fx-text-fill: #D1D5DB; -fx-font-size: 12px; -fx-line-spacing: 2px;");

        this.changelogScrollPane = new ScrollPane(changelogLabel);
        changelogScrollPane.setFitToWidth(true);
        changelogScrollPane.setMaxHeight(160);
        changelogScrollPane.setPrefHeight(130);
        changelogScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: #27272A; -fx-border-width: 1px; -fx-border-radius: 4px; -fx-padding: 8px;");
        changelogScrollPane.setVisible(false);
        changelogScrollPane.setManaged(false);

        // Fortschrittsbalken für Download
        this.progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #38BDF8;");
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        this.lblProgressDetail = new Label();
        lblProgressDetail.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
        lblProgressDetail.setVisible(false);
        lblProgressDetail.setManaged(false);

        // Aktionsbuttons
        this.actionButtonBox = new HBox(8);
        actionButtonBox.setAlignment(Pos.CENTER_RIGHT);

        statusCard.getChildren().addAll(statusRow, changelogScrollPane, progressBar, lblProgressDetail, actionButtonBox);
        return statusCard;
    }

    private HBox buildFooter() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Button btnGithub = new Button("GitHub");
        btnGithub.getStyleClass().add("patch-cancel-btn");
        btnGithub.setGraphic(LucideIcon.createIcon(LucideIcon.EXTERNAL_LINK, Color.web("#9CA3AF"), 0.8));
        btnGithub.setOnAction(e -> OSUtil.openWebpage(UpdateService.REPO_URL));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.btnCloseFooter = new Button("Schließen");
        btnCloseFooter.getStyleClass().add("patch-cancel-btn");
        btnCloseFooter.setOnAction(e -> hide());

        footer.getChildren().addAll(btnGithub, spacer, btnCloseFooter);
        return footer;
    }

    public void show() {
        setVisible(true);
        setManaged(true);
        checkForUpdates();
    }

    public void hide() {
        setVisible(false);
        setManaged(false);
    }

    /**
     * Startet die asynchrone Prüfung gegen die GitHub Releases API.
     */
    public void checkForUpdates() {
        this.isChecking = true;
        this.isError = false;
        spinner.setVisible(true);
        spinner.setManaged(true);
        lblStatus.setText("Suche nach neuen Updates auf GitHub...");
        lblStatus.setStyle("-fx-text-fill: #F3F4F6; -fx-font-size: 12px;");
        lblLatestVersion.setText("Prüfe...");
        lblLatestVersion.setStyle("-fx-text-fill: #38BDF8; -fx-font-size: 14px; -fx-font-weight: bold;");
        changelogScrollPane.setVisible(false);
        changelogScrollPane.setManaged(false);
        progressBar.setVisible(false);
        progressBar.setManaged(false);
        lblProgressDetail.setVisible(false);
        lblProgressDetail.setManaged(false);
        actionButtonBox.getChildren().clear();

        UpdateService.checkLatestRelease()
                .thenAccept(release -> Platform.runLater(() -> applyReleaseResult(release)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> applyErrorResult(ex.getMessage()));
                    return null;
                });
    }

    private void applyReleaseResult(UpdateService.ReleaseInfo release) {
        this.isChecking = false;
        this.isError = false;
        this.lastRelease = release;
        spinner.setVisible(false);
        spinner.setManaged(false);
        lblLatestVersion.setText(release.tagName());

        if (release.isNewer()) {
            lblStatus.setText("Neues Update verfügbar: " + release.tagName());
            lblStatus.setStyle("-fx-text-fill: #38BDF8; -fx-font-size: 12px; -fx-font-weight: bold;");

            if (release.body() != null && !release.body().isBlank()) {
                changelogLabel.setText(release.body());
                changelogScrollPane.setVisible(true);
                changelogScrollPane.setManaged(true);
            }

            this.btnUpdate = new Button("Jetzt aktualisieren");
            btnUpdate.getStyleClass().add("btn-update-primary");
            btnUpdate.setGraphic(LucideIcon.createIcon(LucideIcon.DOWNLOAD, Color.web("#0F172A"), 0.8));

            btnUpdate.setOnAction(e -> {
                btnUpdate.setDisable(true);
                progressBar.setVisible(true);
                progressBar.setManaged(true);
                lblProgressDetail.setVisible(true);
                lblProgressDetail.setManaged(true);
                lblStatus.setText("Lade Update herunter...");

                UpdateService.performUpdate(
                        release,
                        progress -> Platform.runLater(() -> progressBar.setProgress(progress)),
                        status -> Platform.runLater(() -> lblProgressDetail.setText(status))
                ).exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnUpdate.setDisable(false);
                        lblStatus.setText("Update fehlgeschlagen: " + ex.getMessage());
                        lblStatus.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");
                    });
                    return null;
                });
            });

            actionButtonBox.getChildren().setAll(btnUpdate);
        } else {
            lblStatus.setText("Die Anwendung ist auf dem neuesten Stand.");
            lblStatus.setStyle("-fx-text-fill: #34D399; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
    }

    private void applyErrorResult(String error) {
        this.isChecking = false;
        this.isError = true;
        spinner.setVisible(false);
        spinner.setManaged(false);
        lblLatestVersion.setText("Fehler");
        lblLatestVersion.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 14px; -fx-font-weight: bold;");
        lblStatus.setText("Updates konnten nicht geprüft werden (offline oder Rate-Limit).");
        lblStatus.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");

        this.btnRetry = new Button("Wiederholen");
        btnRetry.getStyleClass().add("patch-cancel-btn");
        btnRetry.setGraphic(LucideIcon.createIcon(LucideIcon.REFRESH, Color.web("#E5E7EB"), 0.8));
        btnRetry.setOnAction(e -> checkForUpdates());

        actionButtonBox.getChildren().setAll(btnRetry);
    }
}
