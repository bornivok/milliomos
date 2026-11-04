package hu.milliomos.model;

public class ScoreRecord {
    private String playerName;
    private int answerPoints;
    private int helpPenalty;
    private int remainingSeconds;       // Megtartva a korábbi JSON adatok kompatibilitásához
    private int totalElapsedSeconds;    // Kezdéstől az elért eredményig eltelt összes idő (mp)
    private double timeMultiplier;
    private int finalScore;
    private String playedAt;

    public ScoreRecord() {}

    public ScoreRecord(String playerName, int answerPoints, int helpPenalty,
                       int remainingSeconds, int totalElapsedSeconds,
                       double timeMultiplier, int finalScore, String playedAt) {
        this.playerName = playerName;
        this.answerPoints = answerPoints;
        this.helpPenalty = helpPenalty;
        this.remainingSeconds = remainingSeconds;
        this.totalElapsedSeconds = totalElapsedSeconds;
        this.timeMultiplier = timeMultiplier;
        this.finalScore = finalScore;
        this.playedAt = playedAt;
    }

    public String getPlayerName() { return playerName; }
    public int getAnswerPoints() { return answerPoints; }
    public int getHelpPenalty() { return helpPenalty; }
    public int getRemainingSeconds() { return remainingSeconds; }
    public int getTotalElapsedSeconds() {
        return totalElapsedSeconds > 0 ? totalElapsedSeconds : remainingSeconds;
    }
    public double getTimeMultiplier() { return timeMultiplier; }
    public int getFinalScore() { return finalScore; }
    public String getPlayedAt() { return playedAt; }
}
