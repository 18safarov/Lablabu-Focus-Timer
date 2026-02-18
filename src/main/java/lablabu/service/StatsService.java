package lablabu.service;

import lablabu.model.AppState;
import lablabu.model.Session;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class StatsService {
    private final AppState state;

    public StatsService(AppState state) {
        this.state = state;
    }

    /**
     * Get total time for today (in seconds)
     */
    public long getTodayTotal() {
        String today = LocalDate.now().toString();
        return state.getSessions().stream()
                .filter(s -> s.getDate().equals(today))
                .mapToLong(Session::getDurationSeconds)
                .sum();
    }

    /**
     * Get time for last N days
     */
    public long getLastNDays(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        return state.getSessions().stream()
                .filter(s -> {
                    LocalDate sessionDate = LocalDate.parse(s.getDate());
                    return !sessionDate.isBefore(startDate);
                })
                .mapToLong(Session::getDurationSeconds)
                .sum();
    }

    /**
     * Get time for the week
     */
    public long getWeekTotal() {
        return getLastNDays(7);
    }

    /**
     * Get time for the month
     */
    public long getMonthTotal() {
        return getLastNDays(30);
    }

    /**
     * Get all-time total
     */
    public long getAllTimeTotal() {
        return state.getSessions().stream()
                .mapToLong(Session::getDurationSeconds)
                .sum();
    }

    /**
     * Get statistics by categories (name -> seconds)
     */
    public Map<String, Long> getCategoryStats() {
        Map<String, Long> stats = new HashMap<>();
        for (Session session : state.getSessions()) {
            stats.merge(session.getCategory(), session.getDurationSeconds(), Long::sum);
        }
        return stats;
    }

    /**
     * Calculate current streak
     */
    public void calculateStreak(String today) {
        String lastDateStr = state.getLastSessionDate();
        if (lastDateStr == null || lastDateStr.isEmpty()) {
            state.setStreak(1);
            return;
        }

        LocalDate lastDate = LocalDate.parse(lastDateStr);
        LocalDate todayDate = LocalDate.parse(today);
        long daysBetween = ChronoUnit.DAYS.between(lastDate, todayDate);

        if (daysBetween == 1) {
            // Continue streak
            state.setStreak(state.getStreak() + 1);
        } else if (daysBetween > 1) {
            // Missed days - reset streak
            state.setStreak(1);
        }
        // If daysBetween == 0, there was already a session today, don't change streak
    }

    /**
     * Format seconds to readable format
     */
    public String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
}
