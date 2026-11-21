package de.exit.ytdownloader.core;

import de.exit.ytdownloader.model.AppSettings;
import de.exit.ytdownloader.model.DownloadStatus;
import de.exit.ytdownloader.model.DownloadTask;
import de.exit.ytdownloader.service.SettingsService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueueManager {

    private final DownloadEngine engine;
    private final SettingsService settingsService;
    private final ObservableList<DownloadTask> tasks;
    private final ExecutorService executor;

    public QueueManager(DownloadEngine engine, SettingsService settingsService) {
        this.engine = engine;
        this.settingsService = settingsService;
        this.tasks = FXCollections.observableArrayList();
        this.executor = Executors.newFixedThreadPool(4);
    }

    public ObservableList<DownloadTask> getTasks() {
        return tasks;
    }

    public synchronized void enqueue(DownloadTask task) {
        if (Platform.isFxApplicationThread()) {
            tasks.add(task);
        } else {
            Platform.runLater(() -> tasks.add(task));
        }

        processQueue();
    }

    public synchronized void processQueue() {
        AppSettings settings = settingsService.getSettings();
        int maxConcurrent = settings.getMaxConcurrentDownloads();

        long runningCount = tasks.stream()
                .filter(t -> t.getStatus().isActive())
                .count();

        if (runningCount >= maxConcurrent) {
            return;
        }

        for (DownloadTask task : tasks) {
            if (task.getStatus() == DownloadStatus.QUEUED) {
                startTask(task);
                runningCount++;
                if (runningCount >= maxConcurrent) {
                    break;
                }
            }
        }
    }

    private void startTask(DownloadTask task) {
        Path outputDir = Paths.get(settingsService.getSettings().getDownloadDirectory());
        engine.executeDownloadAsync(task, outputDir).whenComplete((res, ex) -> {
            Platform.runLater(this::processQueue);
        });
    }

    public synchronized void cancelTask(DownloadTask task) {
        task.cancel();
        processQueue();
    }

    public synchronized void removeTask(DownloadTask task) {
        if (task.getStatus().isActive()) {
            task.cancel();
        }
        if (Platform.isFxApplicationThread()) {
            tasks.remove(task);
        } else {
            Platform.runLater(() -> tasks.remove(task));
        }
        processQueue();
    }

    public void shutdown() {
        for (DownloadTask task : tasks) {
            if (task.getStatus().isActive()) {
                task.cancel();
            }
        }
        executor.shutdownNow();
    }
}
