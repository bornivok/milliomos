package hu.milliomos.ui;

import hu.milliomos.model.ScoreRecord;
import hu.milliomos.repository.ScoreRepository;
import hu.milliomos.service.ScoreCalculator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ScoreboardTabView extends BorderPane {
    private final ScoreRepository scoreRepository;
    private final TableView<ScoreRecord> table = new TableView<>();

    public ScoreboardTabView(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
        setPadding(new Insets(15));

        Label title = new Label("🏆 Legyen Ön is Milliomos - Eredménytábla");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setPadding(new Insets(0, 0, 10, 0));
        setTop(title);

        setupColumns();
        setCenter(table);
        refreshTable();
    }

    private void setupColumns() {
        TableColumn<ScoreRecord, String> nameCol = new TableColumn<>("Játékos neve");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPlayerName()));
        nameCol.setPrefWidth(200);

        TableColumn<ScoreRecord, String> prizeCol = new TableColumn<>("Megnyert Nyeremény");
        prizeCol.setCellValueFactory(c -> new SimpleStringProperty(ScoreCalculator.formatFt(c.getValue().getFinalScore())));
        prizeCol.setPrefWidth(240);

        TableColumn<ScoreRecord, String> timeCol = new TableColumn<>("Összes eltelt idő");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(
                ScoreCalculator.formatDuration(c.getValue().getTotalElapsedSeconds())));
        timeCol.setPrefWidth(180);

        TableColumn<ScoreRecord, String> dateCol = new TableColumn<>("Dátum");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPlayedAt()));
        dateCol.setPrefWidth(180);

        table.getColumns().addAll(nameCol, prizeCol, timeCol, dateCol);
    }

    public void refreshTable() {
        table.setItems(FXCollections.observableArrayList(scoreRepository.findAllSorted()));
    }
}
