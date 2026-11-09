package hu.milliomos.model;

import java.util.List;

public class Question {
    public static final int MAX_DIFFICULTY = 7;

    private String questionText;
    private List<String> options;
    private int correctOptionIndex;
    private String tip;
    private List<Integer> probabilities; // 4 elemű lista, pl. [10, 70, 15, 5]
    private int difficulty = 1;          // 1..7 nehézségi szint

    public Question() {}

    public Question(String questionText, List<String> options, int correctOptionIndex,
                    String tip, List<Integer> probabilities) {
        this(questionText, options, correctOptionIndex, tip, probabilities, 1);
    }

    public Question(String questionText, List<String> options, int correctOptionIndex,
                    String tip, List<Integer> probabilities, int difficulty) {
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.tip = tip;
        this.probabilities = probabilities;
        this.difficulty = clampDifficulty(difficulty);
    }

    private int clampDifficulty(int diff) {
        if (diff < 1) return 1;
        if (diff > MAX_DIFFICULTY) return MAX_DIFFICULTY;
        return diff;
    }

    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return options; }
    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public String getTip() { return tip; }
    public List<Integer> getProbabilities() { return probabilities; }
    public int getDifficulty() { return clampDifficulty(difficulty); }
}