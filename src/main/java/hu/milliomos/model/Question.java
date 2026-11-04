package hu.milliomos.model;

import java.util.List;

public class Question {
    private String questionText;
    private List<String> options;
    private int correctOptionIndex;
    private String tip;
    private List<Integer> probabilities; // 4 elemű lista, pl. [10, 70, 15, 5]

    public Question() {}

    public Question(String questionText, List<String> options, int correctOptionIndex, String tip, List<Integer> probabilities) {
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.tip = tip;
        this.probabilities = probabilities;
    }

    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return options; }
    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public String getTip() { return tip; }
    public List<Integer> getProbabilities() { return probabilities; }
}