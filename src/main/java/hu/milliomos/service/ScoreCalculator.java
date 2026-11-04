package hu.milliomos.service;

import hu.milliomos.model.ScoreRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ScoreCalculator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // A valódi "Legyen Ön is milliomos" hivatalos nyereménylétrája (15 szint)
    public static final int[] PRIZES = {
            5_000,
            10_000,
            25_000,
            50_000,
            100_000,   // 5. kérdés: 1. garantált szint (megállás lehetősége)
            200_000,
            300_000,
            500_000,
            800_000,
            1_500_000, // 10. kérdés: 2. garantált szint (megállás lehetősége)
            3_000_000,
            5_000_000,
            10_000_000,
            20_000_000,
            40_000_000 // 15. kérdés: Főnyeremény
    };

    public int calculateQuestionReward(int questionIndex) {
        if (questionIndex < 0 || questionIndex >= PRIZES.length) {
            return 0;
        }
        return PRIZES[questionIndex];
    }

    /**
     * Ezres tagolás szóközzel és "Ft" utótaggal (pl. "100 000 Ft", "40 000 000 Ft")
     */
    public static String formatFt(int amount) {
        return String.format(Locale.US, "%,d", amount).replace(',', ' ') + " Ft";
    }

    /**
     * Ezres tagolás szóközzel Ft nélkül
     */
    public static String formatAmount(int amount) {
        return String.format(Locale.US, "%,d", amount).replace(',', ' ');
    }

    /**
     * Eltelt játékidő formázása (pl. "45 mp", "1 perc", "2 perc 14 mp")
     */
    public static String formatDuration(int totalSeconds) {
        if (totalSeconds < 60) {
            return totalSeconds + " mp";
        }
        int mins = totalSeconds / 60;
        int secs = totalSeconds % 60;
        if (secs == 0) {
            return mins + " perc";
        }
        return String.format("%d perc %02d mp", mins, secs);
    }

    public ScoreRecord createFinalRecord(String playerName, int wonPrize, int helpPenalty, int totalElapsedSeconds) {
        return new ScoreRecord(
                playerName,
                wonPrize,
                0,
                totalElapsedSeconds,
                totalElapsedSeconds,
                1.0,
                wonPrize,
                LocalDateTime.now().format(FORMATTER)
        );
    }
}
