package de.exit.ytdownloader.model;

import java.util.ArrayList;
import java.util.List;

public enum DownloadFormat {
    MP4_BEST("Video Beste Qualität", "bestvideo+bestaudio/best", "mp4", false),
    MP4_1080P("Video 1080p", "bestvideo[height<=1080]+bestaudio/best[height<=1080]", "mp4", false),
    MP4_720P("Video 720p", "bestvideo[height<=720]+bestaudio/best[height<=720]", "mp4", false),
    MP3("Audio MP3", "bestaudio/best", "mp3", true);

    private final String displayName;
    private final String formatSelector;
    private final String fileExtension;
    private final boolean isAudioOnly;

    DownloadFormat(String displayName, String formatSelector, String fileExtension, boolean isAudioOnly) {
        this.displayName = displayName;
        this.formatSelector = formatSelector;
        this.fileExtension = fileExtension;
        this.isAudioOnly = isAudioOnly;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFormatSelector() {
        return formatSelector;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public boolean isAudioOnly() {
        return isAudioOnly;
    }

    /**
     * Erzeugt die yt-dlp Befehlszeilen-Argumente für dieses Format.
     */
    public List<String> buildYtDlpArguments() {
        List<String> args = new ArrayList<>();
        if (isAudioOnly) {
            args.add("-x");
            args.add("--audio-format");
            args.add("mp3");
            args.add("--audio-quality");
            args.add("0");
        } else {
            args.add("-f");
            args.add(formatSelector);
            args.add("--merge-output-format");
            args.add("mp4");
        }
        return args;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
