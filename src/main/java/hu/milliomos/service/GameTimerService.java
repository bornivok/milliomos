package hu.milliomos.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class GameTimerService {
    public static final int QUESTION_TIME_SECONDS = 90; // 90 másodperc minden kérdésre külön-külön

    // Kérdésenkénti visszaszámláló (90 -> 0)
    private final IntegerProperty remainingSeconds = new SimpleIntegerProperty(QUESTION_TIME_SECONDS);
    private Timeline questionTimeline;

    // Háttérben futó teljes játékidő-számláló (0 -> növekszik a kezdéstől a játék végéig)
    private final IntegerProperty totalElapsedSeconds = new SimpleIntegerProperty(0);
    private Timeline totalStopwatchTimeline;
    private long gameStartTimeMs = 0;

    private final SoundEffectService soundEffectService =
            new SoundEffectService();

    public void startQuestionTimer(Runnable onTimeExpired) {
        stopQuestionTimer();

        remainingSeconds.set(QUESTION_TIME_SECONDS);

        questionTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            int current = remainingSeconds.get() - 1;
            remainingSeconds.set(current);

            if (current == 5) {
                soundEffectService.playAcceleratingTick();
            }

            if (current <= 0) {
                stopQuestionTimer();

                if (onTimeExpired != null) {
                    onTimeExpired.run();
                }
            }
        }));

        questionTimeline.setCycleCount(Timeline.INDEFINITE);
        questionTimeline.play();
    }

    public void start(Runnable onTimeExpired) {
        startQuestionTimer(onTimeExpired);
    }

    public void stopQuestionTimer() {
        if (questionTimeline != null) {
            questionTimeline.stop();
        }
    }

    public void stop() {
        stopQuestionTimer();
    }

    public int getRemainingSeconds() {
        return remainingSeconds.get();
    }

    public IntegerProperty remainingSecondsProperty() {
        return remainingSeconds;
    }

    /**
     * Elindítja a kezdéstől a játék végéig mérő háttérbeli időmérőt
     */
    public void startTotalGameStopwatch() {
        stopTotalGameStopwatch();
        totalElapsedSeconds.set(0);
        gameStartTimeMs = System.currentTimeMillis();
        totalStopwatchTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            totalElapsedSeconds.set(totalElapsedSeconds.get() + 1);
        }));
        totalStopwatchTimeline.setCycleCount(Timeline.INDEFINITE);
        totalStopwatchTimeline.play();
    }

    /**
     * Leállítja a háttérbeli teljes játékidő-mérőt
     */
    public void stopTotalGameStopwatch() {
        if (totalStopwatchTimeline != null) {
            totalStopwatchTimeline.stop();
        }
        if (gameStartTimeMs > 0) {
            int actualSecs = (int) ((System.currentTimeMillis() - gameStartTimeMs) / 1000);
            totalElapsedSeconds.set(Math.max(totalElapsedSeconds.get(), actualSecs));
        }
    }

    public int getTotalElapsedSeconds() {
        return totalElapsedSeconds.get();
    }

    public IntegerProperty totalElapsedSecondsProperty() {
        return totalElapsedSeconds;
    }
}
