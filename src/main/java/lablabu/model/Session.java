package lablabu.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Session {
    private String category;
    private long durationSeconds;
    private String date; // Format YYYY-MM-DD
    private String startTime; // ISO format
    private String endTime; // ISO format

    // Constructor for new sessions
    public Session(String category, long durationSeconds, String date) {
        this.category = category;
        this.durationSeconds = durationSeconds;
        this.date = date;
        this.endTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.startTime = LocalDateTime.now().minusSeconds(durationSeconds).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // Full constructor
    public Session(String category, long durationSeconds, String date, String startTime, String endTime) {
        this.category = category;
        this.durationSeconds = durationSeconds;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public String getCategory() { return category; }
    public long getDurationSeconds() { return durationSeconds; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    // Setters
    public void setCategory(String category) { this.category = category; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
    public void setDate(String date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    // Convenient method for formatting duration
    public String getFormattedDuration() {
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        return String.format("%dh %dm", hours, minutes);
    }
}
