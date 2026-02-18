package lablabu.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lablabu.model.AppState;
import lablabu.model.Session;
import lablabu.service.StorageService;
import lablabu.service.TimerService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MainController {

    @FXML private Label timerLabel;
    @FXML private Label statusLabel;
    @FXML private Label streakLabel;
    @FXML private HBox progressDots;
    @FXML private Button startPauseButton;

    private TimerService timerService;
    private final StorageService storageService = new StorageService();
    private AppState state;
    private boolean running = false;

    /**
     * Initialize on window launch
     */
    public void initialize() {
        state = storageService.load();

        // Setup timer: update text and dots every second
        timerService = new TimerService(time -> {
            timerLabel.setText(time);
            updateProgressDots(timerService.getSecondsPassed());
        });

        // Create 8 progress dots (like in Kairu)
        setupProgressDots();

        // Update streak display
        updateStreakUI();
    }

    /**
     * Main Start/Stop button logic
     */
    @FXML
    protected void onToggleSession() {
        if (!running) {
            startSession();
        } else {
            stopAndSaveSession();
        }
    }

    private void startSession() {
        timerService.start();
        running = true;
        startPauseButton.setText("Stop Session");
        statusLabel.setText("Focusing...");
    }

    private void stopAndSaveSession() {
        timerService.stop();
        running = false;
        startPauseButton.setText("Start Focus Session");

        int seconds = timerService.getSecondsPassed();
        if (seconds > 0) {
            saveCurrentSession(seconds);
            statusLabel.setText("Great work! Session saved.");
        } else {
            statusLabel.setText("Session too short to save.");
        }

        timerService.reset();
        updateProgressDots(0); // Reset dots
    }

    /**
     * Save data to JSON
     */
    private void saveCurrentSession(int duration) {
        String today = LocalDate.now().toString();

        calculateStreak(today);

        // Add new session to the list
        Session newSession = new Session("Focus", duration, today);
        state.getSessions().add(newSession);
        state.setLastSessionDate(today);

        storageService.save(state);
        updateStreakUI();
    }

    /**
     * Streak calculation logic (consecutive days)
     */
    private void calculateStreak(String today) {
        String lastDateStr = state.getLastSessionDate();
        if (lastDateStr == null || lastDateStr.isEmpty()) {
            state.setStreak(1);
            return;
        }

        LocalDate lastDate = LocalDate.parse(lastDateStr);
        LocalDate todayDate = LocalDate.parse(today);
        long daysBetween = ChronoUnit.DAYS.between(lastDate, todayDate);

        if (daysBetween == 1) {
            state.setStreak(state.getStreak() + 1);
        } else if (daysBetween > 1) {
            state.setStreak(1); // Reset if a day was skipped
        }
    }

    private void setupProgressDots() {
        progressDots.getChildren().clear();
        for (int i = 0; i < 8; i++) {
            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(6);
            dot.getStyleClass().add("progress-dot");
            dot.setOpacity(0.25); // Dots are dim by default
            progressDots.getChildren().add(dot);
        }
    }

    private void updateProgressDots(int secondsPassed) {
        double fraction = Math.min(1.0, (double) secondsPassed / 1800);
        int activeIndex = (int) Math.floor(fraction * 8);

        for (int i = 0; i < progressDots.getChildren().size(); i++) {
            javafx.scene.shape.Circle dot = (javafx.scene.shape.Circle) progressDots.getChildren().get(i);
            dot.setOpacity(i <= activeIndex ? 1.0 : 0.25);
        }
    }

    private void updateStreakUI() {
        streakLabel.setText("🔥 Streak: " + state.getStreak() + " days");
    }
}
