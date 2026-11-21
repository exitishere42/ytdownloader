package de.exit.ytdownloader.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.exit.ytdownloader.util.OSUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppSettings {

    private String downloadDirectory;
    private DownloadFormat defaultFormat;
    private int maxConcurrentDownloads;

    public AppSettings() {
        this.downloadDirectory = OSUtil.getDefaultDownloadDirectory().toString();
        this.defaultFormat = DownloadFormat.MP4_BEST;
        this.maxConcurrentDownloads = 2;
    }

    public String getDownloadDirectory() {
        return downloadDirectory != null ? downloadDirectory : OSUtil.getDefaultDownloadDirectory().toString();
    }

    public void setDownloadDirectory(String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }

    public DownloadFormat getDefaultFormat() {
        return defaultFormat != null ? defaultFormat : DownloadFormat.MP4_BEST;
    }

    public void setDefaultFormat(DownloadFormat defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    public int getMaxConcurrentDownloads() {
        return maxConcurrentDownloads > 0 ? maxConcurrentDownloads : 2;
    }

    public void setMaxConcurrentDownloads(int maxConcurrentDownloads) {
        this.maxConcurrentDownloads = maxConcurrentDownloads;
    }
}
