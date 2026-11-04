package hu.milliomos.test;

import hu.milliomos.model.ScoreRecord;
import hu.milliomos.service.ScoreCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScoreCalculatorTest {

    private ScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ScoreCalculator();
    }

    @Test
    @DisplayName("A hivatalos Forint értékek pontosak a szinteken")
    void testCalculateQuestionReward() {
        assertEquals(5_000, calculator.calculateQuestionReward(0));      // 1. kérdés
        assertEquals(10_000, calculator.calculateQuestionReward(1));     // 2. kérdés
        assertEquals(100_000, calculator.calculateQuestionReward(4));    // 5. kérdés (1. mérföldkő)
        assertEquals(1_500_000, calculator.calculateQuestionReward(9));  // 10. kérdés (2. mérföldkő)
        assertEquals(40_000_000, calculator.calculateQuestionReward(14)); // 15. kérdés (Főnyeremény)
    }

    @Test
    @DisplayName("Ezres tagolás szóközzel és Ft utótaggal helyesen formáz")
    void testFormatFt() {
        assertEquals("0 Ft", ScoreCalculator.formatFt(0));
        assertEquals("5 000 Ft", ScoreCalculator.formatFt(5000));
        assertEquals("100 000 Ft", ScoreCalculator.formatFt(100000));
        assertEquals("1 500 000 Ft", ScoreCalculator.formatFt(1500000));
        assertEquals("40 000 000 Ft", ScoreCalculator.formatFt(40000000));
    }

    @Test
    @DisplayName("Végső rekord létrehozása: a megnyert összeg a végső pont, segítséglevonás 0, összes eltelt idő helyes")
    void testCreateFinalRecordNormalCase() {
        ScoreRecord record = calculator.createFinalRecord("TesztElek", 1_500_000, 0, 75);

        assertEquals("TesztElek", record.getPlayerName());
        assertEquals(1_500_000, record.getAnswerPoints());
        assertEquals(0, record.getHelpPenalty());
        assertEquals(75, record.getTotalElapsedSeconds());
        assertEquals(75, record.getRemainingSeconds());
        assertEquals(1_500_000, record.getFinalScore());
        assertNotNull(record.getPlayedAt());
    }

    @Test
    @DisplayName("Időtartam formázása helyesen kezeli a másodperceket és perceket")
    void testFormatDuration() {
        assertEquals("0 mp", ScoreCalculator.formatDuration(0));
        assertEquals("45 mp", ScoreCalculator.formatDuration(45));
        assertEquals("1 perc", ScoreCalculator.formatDuration(60));
        assertEquals("2 perc 14 mp", ScoreCalculator.formatDuration(134));
    }
}
