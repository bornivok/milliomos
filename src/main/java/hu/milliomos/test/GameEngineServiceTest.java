package hu.milliomos.test;

import hu.milliomos.model.Question;
import hu.milliomos.model.ScoreRecord;
import hu.milliomos.service.GameEngineService;
import hu.milliomos.service.ScoreCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameEngineServiceTest {

    private GameEngineService gameEngine;
    private List<Question> mockQuestions;

    @BeforeEach
    void setUp() {
        gameEngine = new GameEngineService(new ScoreCalculator());
        mockQuestions = createSampleQuestions(28);
    }

    private List<Question> createSampleQuestions(int count) {
        List<Question> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int difficulty = (i % 7) + 1;
            list.add(new Question(
                    "Teszt kérdés #" + (i + 1),
                    List.of("A", "B", "C", "D"),
                    1,
                    "Tipp szöveg",
                    List.of(10, 70, 10, 10),
                    difficulty
            ));
        }
        return list;
    }

    @Test
    @DisplayName("Kevesebb mint 15 kérdés esetén a játék nem indul el")
    void testStartNewGameFailsWithTooFewQuestions() {
        List<Question> onlyTen = createSampleQuestions(10);
        assertFalse(gameEngine.startNewGame("Jatekos", onlyTen));
    }

    @Test
    @DisplayName("Legalább 15 kérdés esetén a játék sikeresen elindul 0 Ft nyereménnyel")
    void testStartNewGameSucceeds() {
        assertTrue(gameEngine.startNewGame("Jatekos", mockQuestions));
        assertEquals(1, gameEngine.getCurrentQuestionNumber());
        assertEquals(0, gameEngine.getAccumulatedAnswerPoints());
        assertEquals(0, gameEngine.getAccumulatedHelpPenalty());
        assertFalse(gameEngine.canWalkAway());
        assertFalse(gameEngine.isVictory());
    }

    @Test
    @DisplayName("A 15 kérdés kiválasztása a 7 nehézségi szint szerint növekvő sorrendben történik")
    void testQuestionsFollowSevenDifficultyLevels() {
        gameEngine.startNewGame("Jatekos", mockQuestions);

        for (int qIdx = 0; qIdx < 15; qIdx++) {
            int expectedDiff = Math.min(7, (qIdx / 2) + 1);
            Question current = gameEngine.getCurrentQuestion();
            assertEquals(expectedDiff, current.getDifficulty());
            gameEngine.submitAnswer(current.getCorrectOptionIndex());
        }
    }

    @Test
    @DisplayName("Megálláskor megmarad az eddigi összeg, hibás válasznál visszaesik a garantált szintre")
    void testWalkAwayAndMilestoneDrop() {
        gameEngine.startNewGame("Jatekos", mockQuestions);

        for (int i = 0; i < 6; i++) {
            int correctIdx = gameEngine.getCurrentQuestion().getCorrectOptionIndex();
            assertTrue(gameEngine.submitAnswer(correctIdx));
        }
        assertEquals(200_000, gameEngine.getAccumulatedAnswerPoints());
        assertTrue(gameEngine.canWalkAway());

        int wrongIdx = (gameEngine.getCurrentQuestion().getCorrectOptionIndex() + 1) % 4;
        assertFalse(gameEngine.submitAnswer(wrongIdx));
        assertEquals(100_000, gameEngine.getAccumulatedAnswerPoints());
    }

    @Test
    @DisplayName("Segítségek egyszer használhatók és nincs pontlevonás")
    void testLifelinesCanOnlyBeUsedOnceWithoutPenalty() {
        gameEngine.startNewGame("Jatekos", mockQuestions);

        assertNotNull(gameEngine.useTipHelp());
        assertEquals(0, gameEngine.getAccumulatedHelpPenalty());
        assertNull(gameEngine.useTipHelp());

        assertNotNull(gameEngine.useProbabilitiesHelp());
        assertEquals(0, gameEngine.getAccumulatedHelpPenalty());
        assertNull(gameEngine.useProbabilitiesHelp());

        int correctIdx = gameEngine.getCurrentQuestion().getCorrectOptionIndex();
        List<Integer> hidden = gameEngine.useHalvingHelp();
        assertNotNull(hidden);
        assertEquals(2, hidden.size());
        assertFalse(hidden.contains(correctIdx));
        assertEquals(0, gameEngine.getAccumulatedHelpPenalty());
        assertNull(gameEngine.useHalvingHelp());
    }

    @Test
    @DisplayName("15 helyes válasz esetén 40 000 000 Ft a végső nyeremény")
    void testFullVictoryPrize() {
        gameEngine.startNewGame("Jatekos", mockQuestions);

        for (int i = 0; i < 15; i++) {
            gameEngine.submitAnswer(gameEngine.getCurrentQuestion().getCorrectOptionIndex());
        }
        assertTrue(gameEngine.isVictory());

        ScoreRecord winRecord = gameEngine.buildScoreRecord(85);
        assertEquals(40_000_000, winRecord.getAnswerPoints());
        assertEquals(40_000_000, winRecord.getFinalScore());
        assertEquals(0, winRecord.getHelpPenalty());
        assertEquals(85, winRecord.getRemainingSeconds());
    }

    @Test
    @DisplayName("A válaszlehetőségek megkeverése megőrzi a helyes válasz szövegét, a valószínűségeket és a nehézséget")
    void testQuestionOptionsShufflingPreservesCorrectAnswer() {
        Question orig = new Question(
                "Mi Magyarország fővárosa?",
                List.of("Berlin", "Párizs", "Róma", "Budapest"),
                3,
                "Duna partján fekszik",
                List.of(5, 10, 15, 70),
                4
        );
        Question shuffled = GameEngineService.shuffleQuestionOptions(orig);
        int correctIdx = shuffled.getCorrectOptionIndex();

        assertEquals("Budapest", shuffled.getOptions().get(correctIdx));
        assertEquals(70, shuffled.getProbabilities().get(correctIdx));
        assertEquals(4, shuffled.getDifficulty());
        assertEquals(4, shuffled.getOptions().size());
        assertTrue(shuffled.getOptions().containsAll(orig.getOptions()));
    }
}