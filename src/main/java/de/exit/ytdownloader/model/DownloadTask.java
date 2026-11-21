package de.exit.ytdownloader.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

public class DownloadTask {

    private final String id;
    private final String url;
    private String title;
    private String channel;
    private long durationSeconds;
    private String thumbnailUrl;
    private DownloadFormat format;
    private DownloadStatus status;
    private double progress;
    private String speed;
    private String eta;
    private Path outputPath;
    private String errorMessage;
    private final Instant createdAt;

    private transient Process process;
    private transient Consumer<DownloadTask> changeListener;

    public DownloadTask(String url, DownloadFormat format) {
        this.id = UUID.randomUUID().toString();
        this.url = url;
        this.format = format != null ? format : DownloadFormat.MP4_BEST;
        this.status = DownloadStatus.QUEUED;
        this.progress = 0.0;
        this.speed = "";
        this.eta = "";
        this.createdAt = Instant.now();
    }

    public void setMetadata(VideoMetadata metadata) {
        if (metadata != null) {
            this.title = metadata.getTitle();
            this.channel = metadata.getChannel();
            this.durationSeconds = metadata.getDurationSeconds();
            this.thumbnailUrl = metadata.getThumbnailUrl();
            notifyChange();
        }
    }

    public void setChangeListener(Consumer<DownloadTask> listener) {
        this.changeListener = listener;
    }

    private void notifyChange() {
        if (changeListener != null) {
            changeListener.accept(this);
        }
    }

    public void updateProgress(double progress, String speed, String eta) {
        this.progress = Math.max(0.0, Math.min(100.0, progress));
        if (speed != null) this.speed = speed;
        if (eta != null) this.eta = eta;
        notifyChange();
    }

    public void setStatus(DownloadStatus status) {
        this.status = status;
        notifyChange();
    }

    public void cancel() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
        this.status = DownloadStatus.CANCELLED;
        this.errorMessage = "Abgebrochen";
        notifyChange();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getUrl() { return url; }
    public String getTitle() { return title != null ? title : url; }
    public void setTitle(String title) { this.title = title; notifyChange(); }
    public String getChannel() { return channel != null ? channel : ""; }
    public void setChannel(String channel) { this.channel = channel; notifyChange(); }
    public long getDurationSeconds() { return durationSeconds; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public DownloadFormat getFormat() { return format; }
    public void setFormat(DownloadFormat format) { this.format = format; notifyChange(); }
    public DownloadStatus getStatus() { return status; }
    public double getProgress() { return progress; }
    public String getSpeed() { return speed; }
    public String getEta() { return eta; }
    public Path getOutputPath() { return outputPath; }
    public void setOutputPath(Path outputPath) { this.outputPath = outputPath; notifyChange(); }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; notifyChange(); }
    public Instant getCreatedAt() { return createdAt; }

    public Process getProcess() { return process; }
    public void setProcess(Process process) { this.process = process; }
}
