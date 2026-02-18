package lablabu.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.function.Consumer;

public class TimerService {
    public enum TimerMode {
        FREE,       // Free timer (counts up)
        POMODORO,   // Pomodoro (25 min work, 5 min break)
        CUSTOM      // Custom timer
    }

    public enum PomodoroPhase {
        WORK,       // Work phase
        BREAK       // Break phase
    }

    private Timeline timeline;
    private int secondsPassed = 0;
    private Consumer<String> onTick;
    private Runnable onPhaseComplete;

    private TimerMode mode = TimerMode.FREE;
    private PomodoroPhase pomodoroPhase = PomodoroPhase.WORK;

    // Pomodoro durations (constants - don't change)
    private static final int POMODORO_WORK_DURATION = 25 * 60;  // 25 minutes
    private static final int POMODORO_BREAK_DURATION = 5 * 60;   // 5 minutes

    // Custom durations (configurable)
    private int customWorkDuration = 30 * 60;   // Default 30 minutes
    private int customBreakDuration = 10 * 60;  // Default 10 minutes

    private int targetSeconds = 0;

    public TimerService(Consumer<String> onTick) {
        this.onTick = onTick;
        setupTimeline();
    }

    public TimerService(Consumer<String> onTick, Runnable onPhaseComplete) {
        this.onTick = onTick;
        this.onPhaseComplete = onPhaseComplete;
        setupTimeline();
    }

    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void tick() {
        if (mode == TimerMode.FREE) {
            // Free mode - count up
            secondsPassed++;
            onTick.accept(formatTime(secondsPassed));
        } else {
            // Pomodoro and Custom modes - count down
            secondsPassed++;
            int remaining = targetSeconds - secondsPassed;

            if (remaining <= 0) {
                completePhase();
            } else {
                onTick.accept(formatTime(remaining));
            }
        }
    }

    private void completePhase() {
        stop();
        if (onPhaseComplete != null) {
            onPhaseComplete.run();
        }
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    public void reset() {
        timeline.stop();
        secondsPassed = 0;

        if (mode == TimerMode.POMODORO) {
            targetSeconds = (pomodoroPhase == PomodoroPhase.WORK)
                ? POMODORO_WORK_DURATION
                : POMODORO_BREAK_DURATION;
            onTick.accept(formatTime(targetSeconds));
        } else if (mode == TimerMode.CUSTOM) {
            targetSeconds = (pomodoroPhase == PomodoroPhase.WORK)
                ? customWorkDuration
                : customBreakDuration;
            onTick.accept(formatTime(targetSeconds));
        } else {
            onTick.accept("00:00:00");
        }
    }

    public int getSecondsPassed() {
        return secondsPassed;
    }

    // Format time to HH:MM:SS
    private String formatTime(int totalSeconds) {
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    // Timer modes
    public void setMode(TimerMode mode) {
        this.mode = mode;
        reset();
    }

    public TimerMode getMode() {
        return mode;
    }

    public void setPomodoroPhase(PomodoroPhase phase) {
        this.pomodoroPhase = phase;
        if (mode == TimerMode.POMODORO) {
            targetSeconds = (phase == PomodoroPhase.WORK)
                ? POMODORO_WORK_DURATION
                : POMODORO_BREAK_DURATION;
        } else if (mode == TimerMode.CUSTOM) {
            targetSeconds = (phase == PomodoroPhase.WORK)
                ? customWorkDuration
                : customBreakDuration;
        }
        reset();
    }

    public PomodoroPhase getPomodoroPhase() {
        return pomodoroPhase;
    }

    // Switch to next Pomodoro phase
    public void nextPomodoroPhase() {
        if (pomodoroPhase == PomodoroPhase.WORK) {
            setPomodoroPhase(PomodoroPhase.BREAK);
        } else {
            setPomodoroPhase(PomodoroPhase.WORK);
        }
    }

    // Set custom time (in minutes)
    public void setCustomWorkMinutes(int minutes) {
        this.customWorkDuration = minutes * 60;
        if (mode == TimerMode.CUSTOM && pomodoroPhase == PomodoroPhase.WORK) {
            reset();
        }
    }

    public void setCustomBreakMinutes(int minutes) {
        this.customBreakDuration = minutes * 60;
        if (mode == TimerMode.CUSTOM && pomodoroPhase == PomodoroPhase.BREAK) {
            reset();
        }
    }

    public int getCustomWorkMinutes() {
        return customWorkDuration / 60;
    }

    public int getCustomBreakMinutes() {
        return customBreakDuration / 60;
    }
}
