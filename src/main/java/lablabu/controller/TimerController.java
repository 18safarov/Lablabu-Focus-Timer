package lablabu.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lablabu.model.AppState;
import lablabu.model.Category;
import lablabu.model.Session;
import lablabu.service.StatsService;
import lablabu.service.StorageService;
import lablabu.service.TimerService;
import javafx.scene.media.AudioClip;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class TimerController {

    // UI elements
    @FXML private Label timerLabel;
    @FXML private Label phaseLabel;
    @FXML private Label statusLabel;
    @FXML private Label streakLabel;
    @FXML private Label todayLabel;
    @FXML private Button startPauseButton;
    @FXML private VBox sideMenu;
    @FXML private ComboBox<String> modeComboBox;
    @FXML private ListView<String> categoryListView;
    @FXML private TextArea statsTextArea;
    @FXML private RadioButton lightThemeRadio;
    @FXML private RadioButton darkThemeRadio;
    @FXML private TextField workMinutesField;
    @FXML private TextField breakMinutesField;

    // Services
    private TimerService timerService;
    private final StorageService storageService = new StorageService();
    private StatsService statsService;
    private AppState state;
    private AudioClip dingSound;

    private boolean running = false;
    private String selectedCategory = "";
    private boolean menuOpen = false;

    /**
     * Controller initialization
     */
    public void initialize() {
        // Load data
        state = storageService.load();
        statsService = new StatsService(state);

        // Load sound
        try {
            dingSound = new AudioClip(getClass().getResource("/lablabu/ding.mp3").toExternalForm());
            dingSound.setVolume(0.5);
        } catch (Exception e) {
            System.out.println("Sound not loaded");
        }

        // Setup timer with callback for completing Pomodoro phase
        timerService = new TimerService(this::updateTimerDisplay, this::handlePomodoroPhaseComplete);

        // Load saved Custom Timer settings
        int savedWorkMin = state.getPomodoroWorkMinutes();
        int savedBreakMin = state.getPomodoroBreakMinutes();
        timerService.setCustomWorkMinutes(savedWorkMin);
        timerService.setCustomBreakMinutes(savedBreakMin);

        // Set values in fields
        if (workMinutesField != null) {
            workMinutesField.setText(String.valueOf(savedWorkMin));
        }
        if (breakMinutesField != null) {
            breakMinutesField.setText(String.valueOf(savedBreakMin));
        }

        // Populate category list
        updateCategoryList();

        // Setup mode combobox
        modeComboBox.getItems().addAll("Free Timer", "Pomodoro", "Custom Timer");
        modeComboBox.setValue("Free Timer");
        modeComboBox.setOnAction(e -> handleModeChange());

        // Update UI
        updateAllStats();
        applyTheme();

        // Setup theme
        if (state.getTheme().equals("light")) {
            lightThemeRadio.setSelected(true);
        } else {
            darkThemeRadio.setSelected(true);
        }
    }

    /**
     * Update timer display
     */
    private void updateTimerDisplay(String time) {
        Platform.runLater(() -> {
            timerLabel.setText(time);
        });
    }

    /**
     * Handler for Pomodoro/Custom phase completion
     */
    private void handlePomodoroPhaseComplete() {
        Platform.runLater(() -> {
            // Play sound on natural timer completion
            if (dingSound != null) {
                dingSound.play();
            }

            if (timerService.getPomodoroPhase() == TimerService.PomodoroPhase.WORK) {
                // Work complete - save session
                saveCurrentSession();
                statusLabel.setText("✅ Work session complete! Time for a break.");

                // Switch to break
                timerService.nextPomodoroPhase();

                // Update phaseLabel to "Break"
                phaseLabel.setText("Break");

                // Message depends on mode
                if (timerService.getMode() == TimerService.TimerMode.POMODORO) {
                    showAlert("Pomodoro Complete!", "Great work! Take a 5-minute break.", Alert.AlertType.INFORMATION);
                } else {
                    int breakMin = timerService.getCustomBreakMinutes();
                    showAlert("Work Complete!", "Great work! Take a " + breakMin + "-minute break.", Alert.AlertType.INFORMATION);
                }
            } else {
                // Break complete
                statusLabel.setText("Break complete! Ready for another session?");
                timerService.nextPomodoroPhase();

                // Update phaseLabel back to "Focus"
                phaseLabel.setText("Focus");

                showAlert("Break Complete!", "Break is over. Ready to focus again?", Alert.AlertType.INFORMATION);
            }

            running = false;
            startPauseButton.setText("Start");
            updateAllStats();
        });
    }

    /**
     * Start/Pause button
     */
    @FXML
    protected void onToggleSession() {
        if (!running) {
            startSession();
        } else {
            pauseSession();
        }
    }

    /**
     * Start session
     */
    private void startSession() {
        // Check if category is selected
        selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            showAlert("No Category Selected", "Please select a category before starting!", Alert.AlertType.WARNING);
            return;
        }

        timerService.start();
        running = true;
        startPauseButton.setText("Pause");

        // Update label above timer and status
        updatePhaseDisplay();
    }

    /**
     * Update phase display (Focus/Break)
     */
    private void updatePhaseDisplay() {
        TimerService.TimerMode mode = timerService.getMode();
        TimerService.PomodoroPhase phase = timerService.getPomodoroPhase();

        // Update label above timer
        if (mode == TimerService.TimerMode.FREE) {
            phaseLabel.setText("Focus");
        } else {
            // Pomodoro or Custom
            if (phase == TimerService.PomodoroPhase.WORK) {
                phaseLabel.setText("Focus");
            } else {
                phaseLabel.setText("Break");
            }
        }

        // Update status at bottom
        if (mode == TimerService.TimerMode.POMODORO || mode == TimerService.TimerMode.CUSTOM) {
            if (phase == TimerService.PomodoroPhase.WORK) {
                statusLabel.setText("🎯 Focusing on " + selectedCategory + "...");
            } else {
                statusLabel.setText("☕ Break time - relax!");
            }
        } else {
            statusLabel.setText("🎯 Focusing on " + selectedCategory + "...");
        }
    }

    /**
     * Pause
     */
    private void pauseSession() {
        timerService.stop();
        running = false;
        startPauseButton.setText("Resume");
        statusLabel.setText("⏸ Paused");
    }

    /**
     * Open/close side menu
     */
    @FXML
    protected void onToggleMenu() {
        if (sideMenu == null) return;

        menuOpen = !menuOpen;

        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sideMenu);

        if (menuOpen) {
            sideMenu.setVisible(true);
            transition.setToX(0);
        } else {
            transition.setToX(-320);
            transition.setOnFinished(e -> sideMenu.setVisible(false));
        }

        transition.play();
    }

    /**
     * Stop button and save session
     */
    @FXML
    protected void onStopAndSave() {
        if (!running && timerService.getSecondsPassed() == 0) {
            showAlert("No Session", "No active session to save.", Alert.AlertType.INFORMATION);
            return;
        }

        timerService.stop();
        running = false;

        int seconds = timerService.getSecondsPassed();
        if (seconds >= 60) { // Save only if longer than 1 minute
            saveCurrentSession();
            statusLabel.setText("✅ Session saved!");
            showAlert("Session Saved", "Great work! Session saved successfully.", Alert.AlertType.INFORMATION);
        } else {
            statusLabel.setText("⚠️ Session too short to save (min 1 minute)");
        }

        timerService.reset();
        updateAllStats();
        startPauseButton.setText("Start");
        phaseLabel.setText("Focus"); // Reset to Focus
    }

    /**
     * Save current session
     */
    private void saveCurrentSession() {
        String today = LocalDate.now().toString();
        int seconds = timerService.getSecondsPassed();

        // Calculate streak
        statsService.calculateStreak(today);

        // Create and add session
        Session newSession = new Session(selectedCategory, seconds, today);
        state.getSessions().add(newSession);
        state.setLastSessionDate(today);

        // Save
        storageService.save(state);
        updateAllStats();
    }

    /**
     * Change timer mode
     */
    private void handleModeChange() {
        String mode = modeComboBox.getValue();
        if ("Pomodoro".equals(mode)) {
            timerService.setMode(TimerService.TimerMode.POMODORO);
            timerService.setPomodoroPhase(TimerService.PomodoroPhase.WORK);
            statusLabel.setText("Pomodoro mode: 25 min work, 5 min break");
            phaseLabel.setText("Focus");
        } else if ("Custom Timer".equals(mode)) {
            timerService.setMode(TimerService.TimerMode.CUSTOM);
            timerService.setPomodoroPhase(TimerService.PomodoroPhase.WORK);
            int workMin = timerService.getCustomWorkMinutes();
            int breakMin = timerService.getCustomBreakMinutes();
            statusLabel.setText("Custom Timer: " + workMin + " min work, " + breakMin + " min break");
            phaseLabel.setText("Focus");
        } else {
            timerService.setMode(TimerService.TimerMode.FREE);
            statusLabel.setText("Free timer mode");
            phaseLabel.setText("Focus");
        }

        // Reset timer on mode change
        timerService.stop();
        timerService.reset();
        running = false;
        startPauseButton.setText("Start");
    }

    /**
     * Add category
     */
    @FXML
    protected void onAddCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Enter new category name:");
        dialog.setContentText("Category:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Category newCategory = new Category(name.trim());
                state.addCategory(newCategory);
                storageService.save(state);
                updateCategoryList();
            }
        });
    }

    /**
     * Remove category
     */
    @FXML
    protected void onRemoveCategory() {
        String selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Remove Category");
            confirm.setHeaderText("Remove category: " + selected + "?");
            confirm.setContentText("This will not delete associated sessions.");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                state.removeCategory(selected);
                storageService.save(state);
                updateCategoryList();
            }
        }
    }

    /**
     * Update category list
     */
    private void updateCategoryList() {
        categoryListView.getItems().clear();
        for (Category cat : state.getCategories()) {
            categoryListView.getItems().add(cat.getName());
        }

        // Select first category by default
        if (!categoryListView.getItems().isEmpty()) {
            categoryListView.getSelectionModel().select(0);
        }
    }

    /**
     * Update all statistics
     */
    private void updateAllStats() {
        // Streak
        streakLabel.setText("🔥: " + state.getStreak());

        // Today
        long todaySeconds = statsService.getTodayTotal();
        todayLabel.setText("⏱ : " + statsService.formatDuration(todaySeconds));

        // Statistics
        StringBuilder stats = new StringBuilder();
        stats.append("=== Statistics ===\n\n");
        stats.append("Today: ").append(statsService.formatDuration(todaySeconds)).append("\n");
        stats.append("Week: ").append(statsService.formatDuration(statsService.getWeekTotal())).append("\n");
        stats.append("Month: ").append(statsService.formatDuration(statsService.getMonthTotal())).append("\n");
        stats.append("All Time: ").append(statsService.formatDuration(statsService.getAllTimeTotal())).append("\n\n");

        stats.append("=== By Category ===\n\n");
        Map<String, Long> categoryStats = statsService.getCategoryStats();
        for (Map.Entry<String, Long> entry : categoryStats.entrySet()) {
            stats.append(entry.getKey()).append(": ")
                 .append(statsService.formatDuration(entry.getValue())).append("\n");
        }

        if (statsTextArea != null) {
            statsTextArea.setText(stats.toString());
        }
    }


    /**
     * Change theme
     */
    @FXML
    protected void onThemeChange() {
        if (lightThemeRadio.isSelected()) {
            state.setTheme("light");
        } else {
            state.setTheme("dark");
        }
        storageService.save(state);
        applyTheme();
    }

    /**
     * Apply theme - actual CSS switching
     */
    private void applyTheme() {
        String theme = state.getTheme();
        String cssFile = theme.equals("light") ? "theme-light.css" : "theme-dark.css";

        // Get scene from any component
        if (timerLabel != null && timerLabel.getScene() != null) {
            var scene = timerLabel.getScene();
            scene.getStylesheets().clear();
            String cssPath = getClass().getResource("/lablabu/" + cssFile).toExternalForm();
            scene.getStylesheets().add(cssPath);
        }
    }

    /**
     * Show alert
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Apply Custom Timer settings
     */
    @FXML
    protected void onApplyPomodoroSettings() {
        try {
            String workText = workMinutesField.getText();
            String breakText = breakMinutesField.getText();

            if (workText == null || workText.trim().isEmpty()) {
                workText = "30";
            }
            if (breakText == null || breakText.trim().isEmpty()) {
                breakText = "10";
            }

            int workMinutes = Integer.parseInt(workText.trim());
            int breakMinutes = Integer.parseInt(breakText.trim());

            if (workMinutes < 1 || workMinutes > 120) {
                showAlert("Invalid Input", "Work time must be between 1 and 120 minutes", Alert.AlertType.WARNING);
                return;
            }

            if (breakMinutes < 1 || breakMinutes > 60) {
                showAlert("Invalid Input", "Break time must be between 1 and 60 minutes", Alert.AlertType.WARNING);
                return;
            }

            timerService.setCustomWorkMinutes(workMinutes);
            timerService.setCustomBreakMinutes(breakMinutes);

            state.setPomodoroWorkMinutes(workMinutes);
            state.setPomodoroBreakMinutes(breakMinutes);
            storageService.save(state);

            showAlert("Settings Applied",
                "Custom Timer: " + workMinutes + " min work, " + breakMinutes + " min break",
                Alert.AlertType.INFORMATION);

            if ("Custom Timer".equals(modeComboBox.getValue())) {
                timerService.reset();
                statusLabel.setText("Custom Timer: " + workMinutes + " min work, " + breakMinutes + " min break");
            }

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers", Alert.AlertType.ERROR);
        }
    }
}
