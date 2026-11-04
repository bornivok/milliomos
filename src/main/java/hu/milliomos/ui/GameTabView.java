package hu.milliomos.ui;

import hu.milliomos.model.Question;
import hu.milliomos.model.ScoreRecord;
import hu.milliomos.repository.QuestionRepository;
import hu.milliomos.repository.ScoreRepository;
import hu.milliomos.service.GameEngineService;
import hu.milliomos.service.GameTimerService;
import hu.milliomos.service.ScoreCalculator;
import hu.milliomos.service.SoundEffectService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.List;

public class GameTabView extends StackPane {
    private final QuestionRepository questionRepository;
    private final ScoreRepository scoreRepository;
    private final GameEngineService gameEngine;
    private final GameTimerService timerService;
    private final SoundEffectService soundService;
    private final Runnable onScoreSavedCallback;
    private final ScoreCalculator scoreCalculator = new ScoreCalculator();

    private final StudioBackdropPane studioBackdrop = new StudioBackdropPane();
    private final PrizeLadderPane prizeLadderPane = new PrizeLadderPane(scoreCalculator);
    private final AudienceChartPane audienceChartPane = new AudienceChartPane();
    private final VBox registrationPane = new VBox(14);
    private final BorderPane quizPane = new BorderPane();

    private final Label statusLabel = new Label();
    private final Label timerLabel = new Label();
    private final Label questionLabel = new Label();
    private final Label helperOutputLabel = new Label();
    private final Button[] optionButtons = new Button[4];

    // Segítségek - Nincs pontlevonás
    private final Button tipBtn = new Button("💡 Tipp");
    private final Button probBtn = new Button("📊 Esélyek %");
    private final Button halfBtn = new Button("✂ Felező");
    private final Button walkAwayBtn = new Button("💼 Megállok");

    public GameTabView(QuestionRepository questionRepository, ScoreRepository scoreRepository,
                       GameEngineService gameEngine, GameTimerService timerService,
                       SoundEffectService soundService, Runnable onScoreSavedCallback) {
        this.questionRepository = questionRepository;
        this.scoreRepository = scoreRepository;
        this.gameEngine = gameEngine;
        this.timerService = timerService;
        this.soundService = soundService;
        this.onScoreSavedCallback = onScoreSavedCallback;

        buildRegistrationView();
        buildQuizView();

        getChildren().addAll(studioBackdrop, registrationPane, quizPane);
        showRegistration();
    }

    private void buildRegistrationView() {
        registrationPane.setAlignment(Pos.CENTER);
        registrationPane.setMaxSize(490, 260);
        registrationPane.setPadding(new Insets(24, 28, 26, 28));
        registrationPane.getStyleClass().add("glass-panel");

        Label title = new Label("LEGYEN ÖN IS MILLIOMOS!");
        title.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 22));
        title.setStyle("-fx-text-fill: #f5af19;");

        Label subTitle = new Label("15 kérdés, 90 másodperc minden kérdésre.\nFőnyeremény: 40 000 000 Ft!");
        subTitle.setTextAlignment(TextAlignment.CENTER);
        subTitle.setStyle("-fx-text-fill: #c4d7ed; -fx-font-size: 13.5px;");

        TextField nameField = new TextField();
        nameField.setPromptText("Add meg a játékosneved...");
        nameField.setMaxWidth(310);
        nameField.setStyle("-fx-font-size: 14.5px; -fx-background-radius: 0; -fx-border-color: #3b6aa0; -fx-border-width: 1.5; -fx-border-radius: 0; -fx-padding: 8 14; -fx-text-fill: #ffffff; -fx-control-inner-background: #08142c;");

        Button startBtn = new Button("🎬 Játék indítása");
        startBtn.getStyleClass().add("answer-button");
        startBtn.setPrefSize(340, 48);

        startBtn.setOnAction(e -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                showAlert("Hiba", "Kérlek, adj meg egy nevet a játékhoz!");
                return;
            }
            if (!gameEngine.startNewGame(playerName, questionRepository.findAll())) {
                showAlert("Hiányzó kérdések", "Legalább 15 kérdésnek kell lennie a questions.json fájlban!");
                return;
            }
            soundService.playLifeline();
            startQuizSession();
        });

        registrationPane.getChildren().addAll(title, subTitle, nameField, startBtn);
    }

    private void buildQuizView() {
        quizPane.setPadding(new Insets(12, 20, 16, 20));

        // --- FELSŐ HUD SÁV ---
        BorderPane topHud = new BorderPane();
        topHud.getStyleClass().add("glass-panel");
        topHud.setPadding(new Insets(8, 18, 8, 18));

        statusLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");
        timerLabel.setStyle("-fx-text-fill: #f5af19; -fx-font-size: 17px; -fx-font-weight: bold;");

        // Időzítő figyelő: 90 mp kérdésenként
        timerService.remainingSecondsProperty().addListener((obs, oldVal, newVal) -> {
            int secs = newVal.intValue();
            timerLabel.setText("⏱ Idő: " + secs + " mp");
            if (secs <= 15) {
                timerLabel.setStyle("-fx-text-fill: #ff3b30; -fx-font-size: 17px; -fx-font-weight: bold;");
            } else {
                timerLabel.setStyle("-fx-text-fill: #f5af19; -fx-font-size: 17px; -fx-font-weight: bold;");
            }
            if (quizPane.isVisible() && secs > 0) {
                soundService.playTick();
            }
        });

        tipBtn.getStyleClass().add("lifeline-button");
        probBtn.getStyleClass().add("lifeline-button");
        halfBtn.getStyleClass().add("lifeline-button");
        walkAwayBtn.getStyleClass().addAll("lifeline-button", "walk-away-button");

        walkAwayBtn.setOnAction(e -> handleWalkAway());

        HBox lifelinesBox = new HBox(10, tipBtn, probBtn, halfBtn, walkAwayBtn);
        lifelinesBox.setAlignment(Pos.CENTER);

        topHud.setLeft(statusLabel);
        topHud.setCenter(lifelinesBox);
        topHud.setRight(timerLabel);
        BorderPane.setAlignment(statusLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(timerLabel, Pos.CENTER_RIGHT);

        quizPane.setTop(topHud);

        // --- Segítségek eseménykezelői (Pontlevonás NÉLKÜL) ---
        tipBtn.setOnAction(e -> {
            String tip = gameEngine.useTipHelp();
            if (tip != null) {
                soundService.playLifeline();
                helperOutputLabel.setText("💡 Műsorvezetői tipp: " + tip);
                tipBtn.setDisable(true);
                updateHeaderStats();
            }
        });

        probBtn.setOnAction(e -> {
            List<Integer> probs = gameEngine.useProbabilitiesHelp();
            if (probs != null && probs.size() == 4) {
                soundService.playLifeline();
                audienceChartPane.showProbabilities(probs);
                probBtn.setDisable(true);
                updateHeaderStats();
            }
        });

        halfBtn.setOnAction(e -> {
            List<Integer> toHide = gameEngine.useHalvingHelp();
            if (toHide != null) {
                soundService.playLifeline();
                for (int idx : toHide) {
                    optionButtons[idx].setDisable(true);
                }
                halfBtn.setDisable(true);
                updateHeaderStats();
            }
        });

        // --- KÖZÉPSŐ/ALSÓ KVÍZ PULT - SZÖGLETES MEZŐK ÉS HORIZONTÁLIS CSATLAKOZÓ VONALAK ---
        VBox bottomConsole = new VBox(10);
        bottomConsole.setAlignment(Pos.BOTTOM_CENTER);
        bottomConsole.setPadding(new Insets(10, 10, 0, 0));

        helperOutputLabel.setStyle("-fx-text-fill: #00d2ff; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Szögletes kérdés mező: Dinamikusan nő, ha több soros a kérdés
        questionLabel.getStyleClass().add("question-banner");
        questionLabel.setPrefWidth(680);
        questionLabel.setMaxWidth(680);
        questionLabel.setMinHeight(56);
        questionLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
        questionLabel.setMaxHeight(Double.MAX_VALUE);
        questionLabel.setAlignment(Pos.CENTER);
        questionLabel.setTextAlignment(TextAlignment.CENTER);
        questionLabel.setWrapText(true);

        Region qLeftLine = createConnectorLine();
        Region qRightLine = createConnectorLine();
        HBox.setHgrow(qLeftLine, Priority.ALWAYS);
        HBox.setHgrow(qRightLine, Priority.ALWAYS);
        HBox questionRow = new HBox(qLeftLine, questionLabel, qRightLine);
        questionRow.setAlignment(Pos.CENTER);

        // Szögletes válaszgombok: Tágasabb méret, egységes magasság
        for (int i = 0; i < 4; i++) {
            final int index = i;
            Button btn = new Button();
            btn.setPrefWidth(330);
            btn.setMinWidth(310);
            btn.setMaxWidth(350);
            btn.setMinHeight(60);
            btn.setPrefHeight(60);
            btn.setMaxHeight(Double.MAX_VALUE);
            btn.setWrapText(true);
            btn.getStyleClass().add("answer-button");
            btn.setOnAction(e -> handleAnswerSelection(index));
            optionButtons[i] = btn;
        }

        // 1. válaszsor (A és B) horizontális összekötő vonalakkal
        Region r1Left = createConnectorLine();
        Region r1Mid = createConnectorLine();
        Region r1Right = createConnectorLine();
        HBox.setHgrow(r1Left, Priority.ALWAYS);
        HBox.setHgrow(r1Right, Priority.ALWAYS);
        r1Mid.setPrefWidth(16);
        r1Mid.setMinWidth(10);
        HBox row1 = new HBox(r1Left, optionButtons[0], r1Mid, optionButtons[1], r1Right);
        row1.setAlignment(Pos.CENTER);

        // 2. válaszsor (C és D) horizontális összekötő vonalakkal
        Region r2Left = createConnectorLine();
        Region r2Mid = createConnectorLine();
        Region r2Right = createConnectorLine();
        HBox.setHgrow(r2Left, Priority.ALWAYS);
        HBox.setHgrow(r2Right, Priority.ALWAYS);
        r2Mid.setPrefWidth(16);
        r2Mid.setMinWidth(10);
        HBox row2 = new HBox(r2Left, optionButtons[2], r2Mid, optionButtons[3], r2Right);
        row2.setAlignment(Pos.CENTER);

        VBox answersContainer = new VBox(10, row1, row2);
        answersContainer.setAlignment(Pos.CENTER);

        bottomConsole.getChildren().addAll(audienceChartPane, helperOutputLabel, questionRow, answersContainer);
        quizPane.setCenter(bottomConsole);

        // --- JOBB OLDALI 15 LÉPCSŐS NYEREMÉNYFA ---
        BorderPane.setMargin(prizeLadderPane, new Insets(12, 0, 0, 10));
        quizPane.setRight(prizeLadderPane);
    }

    private Region createConnectorLine() {
        Region line = new Region();
        line.setPrefHeight(2);
        line.setMinHeight(2);
        line.setMaxHeight(2);
        line.getStyleClass().add("tv-connector-line");
        return line;
    }

    private void startQuizSession() {
        registrationPane.setVisible(false);
        quizPane.setVisible(true);
        tipBtn.setDisable(false);
        probBtn.setDisable(false);
        halfBtn.setDisable(false);

        timerService.startTotalGameStopwatch();
        loadCurrentQuestionToUI();
    }

    private void loadCurrentQuestionToUI() {
        int currentNum = gameEngine.getCurrentQuestionNumber();
        updateHeaderStats();
        prizeLadderPane.updateCurrentStep(currentNum);
        walkAwayBtn.setDisable(!gameEngine.canWalkAway());
        helperOutputLabel.setText("");
        audienceChartPane.hideChart();

        Question q = gameEngine.getCurrentQuestion();
        questionLabel.setText(currentNum + ". KÉRDÉS: " + q.getQuestionText());

        // Pontos szövegszélesség mérés 15px félkövér betűmérettel
        Text measureText = new Text();
        measureText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        int maxLines = 1;
        String[] prefixes = {"♦ A: ", "♦ B: ", "♦ C: ", "♦ D: "};
        for (int i = 0; i < 4; i++) {
            String optText = q.getOptions().get(i);
            String fullText = prefixes[i] + optText;
            measureText.setText(fullText);
            double w = measureText.getLayoutBounds().getWidth();
            // A gomb belső hasznos szélessége csúcsokkal kb. 245-250px
            if (w > 490.0) {
                maxLines = Math.max(maxLines, 3);
            } else if (w > 245.0) {
                maxLines = Math.max(maxLines, 2);
            }
            optionButtons[i].getStyleClass().removeAll("answer-selected", "answer-correct", "answer-wrong");
            optionButtons[i].setText(fullText);
            optionButtons[i].setDisable(false);
        }

        // Ha bármelyik válasz nem fér ki 1 sorban, mind a 4 mező magassága növekszik egységesen
        double uniformBtnHeight = 54.0;
        if (maxLines >= 3) {
            uniformBtnHeight = 92.0;
        } else if (maxLines == 2) {
            uniformBtnHeight = 72.0;
        }
        for (Button btn : optionButtons) {
            btn.setMinHeight(uniformBtnHeight);
            btn.setPrefHeight(uniformBtnHeight);
            btn.setMaxHeight(uniformBtnHeight);
        }

        // 90 másodperc minden kérdésre külön-külön
        timerService.startQuestionTimer(() -> Platform.runLater(this::handleQuestionTimeout));
    }

    private void handleQuestionTimeout() {
        soundService.playWrong();
        gameEngine.applyWrongAnswerDrop();
        finishGame("Lejárt a 90 másodperces időkorlát erre a kérdésre! Visszaestél a legutóbbi garantált nyereményre.");
    }

    private void handleWalkAway() {
        if (!gameEngine.canWalkAway()) return;

        timerService.stop();
        walkAwayBtn.setDisable(true);
        for (Button btn : optionButtons) {
            btn.setDisable(true);
        }

        int correctIndex = gameEngine.getCurrentQuestion().getCorrectOptionIndex();
        optionButtons[correctIndex].getStyleClass().add("answer-correct");
        soundService.playWalkAway();

        PauseTransition revealPause = new PauseTransition(Duration.seconds(0.8));
        revealPause.setOnFinished(e -> finishGame("Megálltál! Biztonságban elvitted az eddig megszerzett nyereményt."));
        revealPause.play();
    }

    private void handleAnswerSelection(int selectedIndex) {
        // A válasz megjelölésekor az óra azonnal megáll
        timerService.stop();
        walkAwayBtn.setDisable(true);
        for (Button btn : optionButtons) {
            btn.setDisable(true);
        }

        int correctIndex = gameEngine.getCurrentQuestion().getCorrectOptionIndex();
        Button selectedBtn = optionButtons[selectedIndex];
        selectedBtn.getStyleClass().add("answer-selected");
        soundService.playSelect();

        PauseTransition suspensePause = new PauseTransition(Duration.seconds(0.7));
        suspensePause.setOnFinished(e -> {
            selectedBtn.getStyleClass().remove("answer-selected");
            boolean correct = gameEngine.submitAnswer(selectedIndex);

            if (correct) {
                selectedBtn.getStyleClass().add("answer-correct");
                if (gameEngine.isVictory()) {
                    prizeLadderPane.updateCurrentStep(GameEngineService.MAX_QUESTIONS + 1);
                    soundService.playVictory();
                } else {
                    soundService.playCorrect();
                }
            } else {
                selectedBtn.getStyleClass().add("answer-wrong");
                optionButtons[correctIndex].getStyleClass().add("answer-correct");
                soundService.playWrong();
            }

            PauseTransition resultPause = new PauseTransition(Duration.seconds(0.85));
            resultPause.setOnFinished(ev -> {
                if (!quizPane.isVisible()) return;
                if (!correct) {
                    finishGame("Helytelen válasz! Visszaestél a legutóbbi garantált nyereményre.");
                } else if (gameEngine.isVictory()) {
                    finishGame("Gratulálunk! Mind a 15 kérdésre helyesen válaszoltál és megnyerted a 40 000 000 Ft főnyereményt!");
                } else {
                    loadCurrentQuestionToUI();
                }
            });
            resultPause.play();
        });
        suspensePause.play();
    }

    private void updateHeaderStats() {
        int currentNum = Math.min(15, gameEngine.getCurrentQuestionNumber());
        int currentStake = scoreCalculator.calculateQuestionReward(currentNum - 1);
        int currentWon = gameEngine.getAccumulatedAnswerPoints();
        statusLabel.setText(String.format("Kérdés: %d/15   |   Eddigi nyeremény: %s   |   Kérdés tétje: %s",
                currentNum,
                ScoreCalculator.formatFt(currentWon),
                ScoreCalculator.formatFt(currentStake)));
    }

    private void finishGame(String titleMessage) {
        timerService.stop();
        timerService.stopTotalGameStopwatch();
        int totalElapsed = timerService.getTotalElapsedSeconds();

        ScoreRecord record = gameEngine.buildScoreRecord(totalElapsed);
        scoreRepository.saveScore(record);
        onScoreSavedCallback.run();

        int wonPrize = record.getFinalScore();
        String summary = String.format(
                "%s\n\n" +
                        "Megnyert nyeremény: %s\n" +
                        "Megválaszolt kérdések: %d / 15\n" +
                        "Összes eltelt játékidő: %s\n" +
                        "-----------------------------------\n" +
                        "VÉGSŐ NYEREMÉNY: %s",
                titleMessage,
                ScoreCalculator.formatFt(wonPrize),
                Math.max(0, gameEngine.getCurrentQuestionNumber() - 1),
                ScoreCalculator.formatDuration(totalElapsed),
                ScoreCalculator.formatFt(wonPrize)
        );

        Platform.runLater(() -> {
            showAlert("Játék vége - Összesítés", summary);
            showRegistration();
        });
    }

    private void showRegistration() {
        quizPane.setVisible(false);
        registrationPane.setVisible(true);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
