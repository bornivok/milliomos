package hu.milliomos.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.Collections;
import java.util.List;

public class AudienceChartPane extends VBox {
    private static final double MAX_BAR_HEIGHT = 85.0;

    private final Label[] percentLabels = new Label[4];
    private final Region[] barFills = new Region[4];

    public AudienceChartPane() {
        setSpacing(6);
        setPadding(new Insets(8, 16, 8, 16));
        setMaxSize(300, 155);
        setAlignment(Pos.CENTER);
        getStyleClass().addAll("glass-panel", "audience-chart-pane");

        Label title = new Label("📊 KÖZÖNSÉG SZAVAZÁS");
        title.setStyle("-fx-text-fill: #00d2ff; -fx-font-weight: bold; -fx-font-size: 12.5px;");

        HBox barsContainer = new HBox(20);
        barsContainer.setAlignment(Pos.BOTTOM_CENTER);
        barsContainer.setPadding(new Insets(2, 0, 0, 0));

        String[] optionLetters = {"♦ A", "♦ B", "♦ C", "♦ D"};

        for (int i = 0; i < 4; i++) {
            VBox column = new VBox(3);
            column.setAlignment(Pos.BOTTOM_CENTER);

            Label pctLabel = new Label("0%");
            pctLabel.getStyleClass().add("chart-pct-label");
            percentLabels[i] = pctLabel;

            StackPane barTrack = new StackPane();
            barTrack.setPrefSize(36, MAX_BAR_HEIGHT);
            barTrack.setMaxSize(36, MAX_BAR_HEIGHT);
            barTrack.setAlignment(Pos.BOTTOM_CENTER);
            barTrack.getStyleClass().add("chart-bar-track");

            Region barFill = new Region();
            barFill.setPrefWidth(32);
            barFill.setMaxWidth(32);
            barFill.setPrefHeight(0);
            barFill.setMaxHeight(0);
            barFill.getStyleClass().add("chart-bar-fill");
            barFills[i] = barFill;

            barTrack.getChildren().add(barFill);

            Label axisLabel = new Label(optionLetters[i]);
            axisLabel.getStyleClass().add("chart-axis-label");

            column.getChildren().addAll(pctLabel, barTrack, axisLabel);
            barsContainer.getChildren().add(column);
        }

        getChildren().addAll(title, barsContainer);
        hideChart();
    }

    public void showProbabilities(List<Integer> probabilities) {
        if (probabilities == null || probabilities.size() != 4) return;

        setVisible(true);
        setManaged(true);

        int maxProb = Collections.max(probabilities);
        Timeline timeline = new Timeline();

        for (int i = 0; i < 4; i++) {
            int targetPct = probabilities.get(i);
            double targetHeight = Math.max(4.0, (targetPct / 100.0) * MAX_BAR_HEIGHT);

            Region bar = barFills[i];
            Label pctLabel = percentLabels[i];

            bar.getStyleClass().removeAll("chart-bar-highest");
            if (targetPct == maxProb && maxProb > 0) {
                bar.getStyleClass().add("chart-bar-highest");
            }

            bar.setPrefHeight(0);
            bar.setMaxHeight(0);
            pctLabel.setText(targetPct + "%");

            KeyValue kvPref = new KeyValue(bar.prefHeightProperty(), targetHeight, Interpolator.EASE_OUT);
            KeyValue kvMax = new KeyValue(bar.maxHeightProperty(), targetHeight, Interpolator.EASE_OUT);

            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(750), kvPref, kvMax));
        }

        timeline.play();
    }

    public void hideChart() {
        setVisible(false);
        setManaged(false);
    }
}