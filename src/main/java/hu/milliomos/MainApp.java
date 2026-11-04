package hu.milliomos;

import hu.milliomos.repository.QuestionRepository;
import hu.milliomos.repository.ScoreRepository;
import hu.milliomos.service.GameEngineService;
import hu.milliomos.service.GameTimerService;
import hu.milliomos.service.ScoreCalculator;
import hu.milliomos.service.SoundEffectService;
import hu.milliomos.ui.EditorTabView;
import hu.milliomos.ui.GameTabView;
import hu.milliomos.ui.ScoreboardTabView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        QuestionRepository questionRepository = new QuestionRepository();
        ScoreRepository scoreRepository = new ScoreRepository();
        ScoreCalculator scoreCalculator = new ScoreCalculator();
        GameTimerService timerService = new GameTimerService();
        GameEngineService gameEngine = new GameEngineService(scoreCalculator);
        SoundEffectService soundService = new SoundEffectService();

        ScoreboardTabView scoreboardView = new ScoreboardTabView(scoreRepository);
        GameTabView gameView = new GameTabView(
                questionRepository,
                scoreRepository,
                gameEngine,
                timerService,
                soundService,
                scoreboardView::refreshTable
        );
        EditorTabView editorView = new EditorTabView(questionRepository);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                new Tab("Játék", gameView),
                new Tab("Eredmények", scoreboardView),
                new Tab("Kérdésszerkesztő", editorView)
        );

        // Felső menüsávban lévő szögletes némító gomb
        Button topMuteBtn = new Button("🔊");
        topMuteBtn.getStyleClass().add("top-bar-mute-btn");
        topMuteBtn.setTooltip(new Tooltip("Hang némítása / visszakapcsolása"));
        topMuteBtn.setOnAction(e -> {
            boolean newMuted = !soundService.isMuted();
            soundService.setMuted(newMuted);
            topMuteBtn.setText(newMuted ? "🔇" : "🔊");
        });

        StackPane root = new StackPane();
        root.getChildren().addAll(tabPane, topMuteBtn);
        StackPane.setAlignment(topMuteBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(topMuteBtn, new Insets(5, 14, 0, 0));

        Scene scene = new Scene(root, 960, 650);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setTitle("Legyen Ön is Milliomos - JavaFX Desktop");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> timerService.stop());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
