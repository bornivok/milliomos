package hu.milliomos.service;

import hu.milliomos.model.Question;
import hu.milliomos.model.ScoreRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngineService {
    public static final int MAX_QUESTIONS = 15;
    public static final int PENALTY_TIP = 0;
    public static final int PENALTY_PROBABILITIES = 0;
    public static final int PENALTY_HALVING = 0;

    public static final int MILESTONE_1_PRIZE = 100_000;   // 5. kérdés (1. garantált szint)
    public static final int MILESTONE_2_PRIZE = 1_500_000; // 10. kérdés (2. garantált szint)
    public static final int GRAND_PRIZE = 40_000_000;      // 15. kérdés (főnyeremény)

    private final ScoreCalculator scoreCalculator;
    private List<Question> activeQuestions = new ArrayList<>();
    private String currentPlayerName;
    private int currentQuestionIndex;
    private int accumulatedAnswerPoints; // Eddig megszerzett nyeremény (Ft)
    private int accumulatedHelpPenalty;  // 0, mert nincs pontlevonás

    private boolean tipUsed;
    private boolean probabilitiesUsed;
    private boolean halvingUsed;

    public GameEngineService(ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    public boolean startNewGame(String playerName, List<Question> allQuestions) {
        if (allQuestions.size() < MAX_QUESTIONS) {
            return false;
        }
        this.currentPlayerName = playerName;
        this.currentQuestionIndex = 0;
        this.accumulatedAnswerPoints = 0;
        this.accumulatedHelpPenalty = 0;
        this.tipUsed = false;
        this.probabilitiesUsed = false;
        this.halvingUsed = false;

        // 15 véletlenszerű kérdés kiválasztása
        List<Question> shuffledPool = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffledPool);

        // Minden kérdésnél a 4 válaszlehetőség sorrendjének véletlenszerű megkeverése
        this.activeQuestions = new ArrayList<>(MAX_QUESTIONS);
        for (int i = 0; i < MAX_QUESTIONS; i++) {
            this.activeQuestions.add(shuffleQuestionOptions(shuffledPool.get(i)));
        }
        return true;
    }

    /**
     * Véletlenszerűen megkeveri a 4 válaszlehetőség helyét, és megfelelően átigazítja
     * a helyes válasz indexét és a közönség szavazati arányait.
     */
    public static Question shuffleQuestionOptions(Question original) {
        if (original == null || original.getOptions() == null || original.getOptions().size() != 4) {
            return original;
        }

        List<Integer> indices = new ArrayList<>(List.of(0, 1, 2, 3));
        Collections.shuffle(indices);

        List<String> newOptions = new ArrayList<>(4);
        List<Integer> newProbs = new ArrayList<>(4);
        int newCorrectIndex = 0;

        for (int newPos = 0; newPos < 4; newPos++) {
            int origIdx = indices.get(newPos);
            newOptions.add(original.getOptions().get(origIdx));
            if (origIdx == original.getCorrectOptionIndex()) {
                newCorrectIndex = newPos;
            }
            if (original.getProbabilities() != null && original.getProbabilities().size() == 4) {
                newProbs.add(original.getProbabilities().get(origIdx));
            }
        }

        if (newProbs.size() != 4) {
            newProbs = original.getProbabilities();
        }

        return new Question(
                original.getQuestionText(),
                newOptions,
                newCorrectIndex,
                original.getTip(),
                newProbs
        );
    }

    public Question getCurrentQuestion() {
        return activeQuestions.get(currentQuestionIndex);
    }

    public boolean submitAnswer(int selectedOptionIndex) {
        Question current = getCurrentQuestion();
        if (current.getCorrectOptionIndex() == selectedOptionIndex) {
            accumulatedAnswerPoints = scoreCalculator.calculateQuestionReward(currentQuestionIndex);
            currentQuestionIndex++;
            return true;
        }
        applyWrongAnswerDrop();
        return false;
    }

    /**
     * Hibás válasz esetén visszaesés a garantált mérföldkőre:
     * - 1-5. kérdés elrontásakor: 0 Ft
     * - 6-10. kérdés elrontásakor: 100 000 Ft
     * - 11-15. kérdés elrontásakor: 1 500 000 Ft
     */
    public void applyWrongAnswerDrop() {
        if (currentQuestionIndex < 5) {
            this.accumulatedAnswerPoints = 0;
        } else if (currentQuestionIndex < 10) {
            this.accumulatedAnswerPoints = MILESTONE_1_PRIZE;
        } else {
            this.accumulatedAnswerPoints = MILESTONE_2_PRIZE;
        }
    }

    public boolean canWalkAway() {
        return currentQuestionIndex > 0 && currentQuestionIndex < MAX_QUESTIONS;
    }

    public boolean isVictory() {
        return currentQuestionIndex >= MAX_QUESTIONS;
    }

    public String useTipHelp() {
        if (tipUsed) return null;
        tipUsed = true;
        return getCurrentQuestion().getTip();
    }

    public List<Integer> useProbabilitiesHelp() {
        if (probabilitiesUsed) return null;
        probabilitiesUsed = true;
        return getCurrentQuestion().getProbabilities();
    }

    public List<Integer> useHalvingHelp() {
        if (halvingUsed) return null;
        halvingUsed = true;

        int correct = getCurrentQuestion().getCorrectOptionIndex();
        List<Integer> wrongIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i != correct) wrongIndices.add(i);
        }
        Collections.shuffle(wrongIndices);
        return wrongIndices.subList(0, 2);
    }

    public ScoreRecord buildScoreRecord(int totalElapsedSeconds) {
        int finalPrize = accumulatedAnswerPoints;
        return scoreCalculator.createFinalRecord(
                currentPlayerName,
                finalPrize,
                0,
                totalElapsedSeconds
        );
    }

    public int getCurrentQuestionNumber() { return currentQuestionIndex + 1; }
    public int getAccumulatedAnswerPoints() { return accumulatedAnswerPoints; }
    public int getAccumulatedHelpPenalty() { return 0; }
}
