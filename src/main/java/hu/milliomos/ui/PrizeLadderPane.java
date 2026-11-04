package hu.milliomos.ui;

import hu.milliomos.service.GameEngineService;
import hu.milliomos.service.ScoreCalculator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * 15 lépcsős nyereményfa a hivatalos Forint értékekkel:
 * 5. és 10. szint: Garantált mérföldkő (100 000 Ft, 1 500 000 Ft)
 * 15. szint: Főnyeremény (40 000 000 Ft)
 */
public class PrizeLadderPane extends VBox {
    private final HBox[] stepRows = new HBox[GameEngineService.MAX_QUESTIONS];

    public PrizeLadderPane(ScoreCalculator scoreCalculator) {
        setSpacing(3.5);
        setPadding(new Insets(10, 12, 10, 12));
        setPrefWidth(215);
        setMaxWidth(215);
        setAlignment(Pos.CENTER);
        getStyleClass().addAll("glass-panel", "prize-ladder");

        Label header = new Label("🏆 NYEREMÉNYFA");
        header.setStyle("-fx-text-fill: #f5af19; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 0 0 4 0;");
        getChildren().add(header);

        for (int i = GameEngineService.MAX_QUESTIONS - 1; i >= 0; i--) {
            int questionNum = i + 1;
            int reward = scoreCalculator.calculateQuestionReward(i);
            boolean isMilestone = (questionNum == 5 || questionNum == 10 || questionNum == 15);

            HBox row = new HBox(6);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(2, 8, 2, 8));
            row.getStyleClass().add("ladder-step");
            if (isMilestone) {
                row.getStyleClass().add("ladder-step-milestone");
            }

            Label numLabel = new Label(String.format("%2d.", questionNum));
            numLabel.getStyleClass().add("ladder-text");

            Label diamondLabel = new Label("♦");
            diamondLabel.setStyle(isMilestone
                    ? "-fx-text-fill: #ffffff; -fx-font-size: 10px;"
                    : "-fx-text-fill: #f5af19; -fx-font-size: 10px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label pointsLabel = new Label(ScoreCalculator.formatFt(reward));
            pointsLabel.getStyleClass().add("ladder-text");

            row.getChildren().addAll(numLabel, diamondLabel, spacer, pointsLabel);
            stepRows[i] = row;
            getChildren().add(row);
        }
    }

    public void updateCurrentStep(int currentQuestionNumber) {
        int currentIndex = currentQuestionNumber - 1;
        for (int i = 0; i < GameEngineService.MAX_QUESTIONS; i++) {
            HBox row = stepRows[i];
            row.getStyleClass().removeAll("ladder-step-current", "ladder-step-passed");

            if (i < currentIndex) {
                row.getStyleClass().add("ladder-step-passed");
            } else if (i == currentIndex) {
                row.getStyleClass().add("ladder-step-current");
            }
        }
    }
}
