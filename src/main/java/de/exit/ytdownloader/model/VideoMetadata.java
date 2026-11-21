package de.exit.ytdownloader.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoMetadata {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("uploader")
    private String channel;

    @JsonProperty("duration")
    private long durationSeconds;

    @JsonProperty("thumbnail")
    private String thumbnailUrl;

    @JsonProperty("webpage_url")
    private String webpageUrl;

    public VideoMetadata() {}

    public VideoMetadata(String id, String title, String channel, long durationSeconds, String thumbnailUrl, String webpageUrl) {
        this.id = id;
        this.title = title;
        this.channel = channel;
        this.durationSeconds = durationSeconds;
        this.thumbnailUrl = thumbnailUrl;
        this.webpageUrl = webpageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "Unbekannter Titel";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChannel() {
        return channel != null ? channel : "Unbekannter Kanal";
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getWebpageUrl() {
        return webpageUrl;
    }

    public void setWebpageUrl(String webpageUrl) {
        this.webpageUrl = webpageUrl;
    }

    public String getFormattedDuration() {
        if (durationSeconds <= 0) {
            return "00:00";
        }
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
