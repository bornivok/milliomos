package hu.milliomos.ui;

import hu.milliomos.model.Question;
import hu.milliomos.repository.QuestionRepository;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Arrays;
import java.util.List;

public class EditorTabView extends BorderPane {
    private final QuestionRepository questionRepository;
    private final ListView<Question> questionListView = new ListView<>();

    private final TextField questionField = new TextField();
    private final TextField[] optionFields = new TextField[4];
    @SuppressWarnings("unchecked")
    private final Spinner<Integer>[] probSpinners = new Spinner[4];
    private final ComboBox<String> correctCombo = new ComboBox<>();
    private final TextField tipField = new TextField();

    private final Button updateBtn = new Button("✏️ Kijelölt módosítása");
    private final Button deleteBtn = new Button("🗑️ Kijelölt törlése");

    public EditorTabView(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
        setPadding(new Insets(18));

        Label title = new Label("Kérdésszerkesztő");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #f5af19;");
        BorderPane.setMargin(title, new Insets(0, 0, 12, 0));
        setTop(title);

        setLeft(buildQuestionListPane());
        setCenter(buildEditorFormPane());

        refreshQuestionList();
    }

    private VBox buildQuestionListPane() {
        VBox leftBox = new VBox(10);
        leftBox.setPrefWidth(320);
        leftBox.setPadding(new Insets(0, 15, 0, 0));

        Label listHeader = new Label("Meglévő kérdések:");
        listHeader.setStyle("-fx-text-fill: #b0c4de; -fx-font-weight: bold;");

        questionListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText((getIndex() + 1) + ". " + item.getQuestionText());
                }
            }
        });

        questionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedQuestion) -> {
            boolean hasSelection = (selectedQuestion != null);
            updateBtn.setDisable(!hasSelection);
            deleteBtn.setDisable(!hasSelection);
            if (hasSelection) {
                populateForm(selectedQuestion);
            }
        });

        VBox.setVgrow(questionListView, Priority.ALWAYS);

        Button clearSelectionBtn = new Button("🔄 Űrlap ürítése (Új kérdés mód)");
        clearSelectionBtn.setMaxWidth(Double.MAX_VALUE);
        clearSelectionBtn.setOnAction(e -> clearForm());

        leftBox.getChildren().addAll(listHeader, questionListView, clearSelectionBtn);
        return leftBox;
    }

    private VBox buildEditorFormPane() {
        VBox rightBox = new VBox(15);
        rightBox.getStyleClass().add("glass-panel");
        rightBox.setPadding(new Insets(20));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);

        questionField.setPrefWidth(360);
        questionField.setPromptText("Írd be a kérdés szövegét...");

        String[] labels = {"A válasz:", "B válasz:", "C válasz:", "D válasz:"};
        int[] defaultProbs = {40, 25, 20, 15};

        form.add(new Label("Kérdés szövege:"), 0, 0);
        form.add(questionField, 1, 0, 3, 1);

        for (int i = 0; i < 4; i++) {
            optionFields[i] = new TextField();
            Spinner<Integer> spinner = new Spinner<>(0, 100, defaultProbs[i]);
            spinner.setEditable(true);
            spinner.setPrefWidth(80);
            spinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) spinner.increment(0);
            });
            probSpinners[i] = spinner;

            form.add(new Label(labels[i]), 0, i + 1);
            form.add(optionFields[i], 1, i + 1);
            form.add(new Label("Esély %:"), 2, i + 1);
            form.add(probSpinners[i], 3, i + 1);
        }

        correctCombo.getItems().addAll("0 (A válasz)", "1 (B válasz)", "2 (C válasz)", "3 (D válasz)");
        correctCombo.getSelectionModel().selectFirst();

        tipField.setPromptText("Rövid szöveges segítség a játékosnak...");

        form.add(new Label("Helyes válasz:"), 0, 5);
        form.add(correctCombo, 1, 5);
        form.add(new Label("Kisebb tipp:"), 0, 6);
        form.add(tipField, 1, 6, 3, 1);

        Button addNewBtn = new Button("➕ Mentés új kérdésként");
        addNewBtn.getStyleClass().add("lifeline-button");

        updateBtn.getStyleClass().add("lifeline-button");
        updateBtn.setDisable(true);

        deleteBtn.getStyleClass().add("lifeline-button");
        deleteBtn.setStyle("-fx-border-color: #e74c3c; -fx-text-fill: #ffb3b3;");
        deleteBtn.setDisable(true);

        addNewBtn.setOnAction(e -> handleSaveNew());
        updateBtn.setOnAction(e -> handleUpdateSelected());
        deleteBtn.setOnAction(e -> handleDeleteSelected());

        HBox actionButtons = new HBox(12, addNewBtn, updateBtn, deleteBtn);
        actionButtons.setAlignment(Pos.CENTER_LEFT);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        rightBox.getChildren().addAll(form, actionButtons);
        return rightBox;
    }

    private void populateForm(Question q) {
        questionField.setText(q.getQuestionText());
        tipField.setText(q.getTip());
        correctCombo.getSelectionModel().select(q.getCorrectOptionIndex());

        for (int i = 0; i < 4; i++) {
            optionFields[i].setText(q.getOptions().get(i));
            probSpinners[i].getValueFactory().setValue(q.getProbabilities().get(i));
        }
    }

    private Question validateAndBuildQuestion() {
        String qText = questionField.getText().trim();
        String tipText = tipField.getText().trim();
        List<String> opts = Arrays.stream(optionFields).map(t -> t.getText().trim()).toList();
        List<Integer> probs = Arrays.stream(probSpinners).map(Spinner::getValue).toList();

        if (qText.isEmpty() || tipText.isEmpty() || opts.stream().anyMatch(String::isEmpty)) {
            showAlert(Alert.AlertType.WARNING, "Hiányzó adat", "Minden mezőt kötelező kitölteni!");
            return null;
        }

        int probSum = probs.stream().mapToInt(Integer::intValue).sum();
        if (probSum != 100) {
            showAlert(Alert.AlertType.WARNING, "Hibás százalékok",
                    "A 4 válasz esélyeinek összege pontosan 100% kell legyen! (Jelenlegi: " + probSum + "%)");
            return null;
        }

        int correctIdx = correctCombo.getSelectionModel().getSelectedIndex();
        return new Question(qText, opts, correctIdx, tipText, probs);
    }

    private void handleSaveNew() {
        Question newQuestion = validateAndBuildQuestion();
        if (newQuestion == null) return;

        questionRepository.saveNewQuestion(newQuestion);
        refreshQuestionList();
        clearForm();
        showAlert(Alert.AlertType.INFORMATION, "Siker", "Az új kérdés bekerült a questions.json fájlba!");
    }

    private void handleUpdateSelected() {
        int selectedIdx = questionListView.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0) return;

        Question updatedQuestion = validateAndBuildQuestion();
        if (updatedQuestion == null) return;

        questionRepository.updateQuestion(selectedIdx, updatedQuestion);
        refreshQuestionList();
        questionListView.getSelectionModel().select(selectedIdx);
        showAlert(Alert.AlertType.INFORMATION, "Frissítve", "A kijelölt kérdés módosításai elmentve!");
    }

    private void handleDeleteSelected() {
        int selectedIdx = questionListView.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0) return;

        if (questionRepository.findAll().size() <= 15) {
            showAlert(Alert.AlertType.WARNING, "Törlés megtagadva",
                    "Nem törölhetsz több kérdést, mert a játékhoz legalább 15 kérdés szükséges!");
            return;
        }

        questionRepository.deleteQuestion(selectedIdx);
        refreshQuestionList();
        clearForm();
        showAlert(Alert.AlertType.INFORMATION, "Törölve", "A kijelölt kérdés törölve lett a JSON fájlból.");
    }

    private void refreshQuestionList() {
        questionListView.setItems(FXCollections.observableArrayList(questionRepository.findAll()));
    }

    private void clearForm() {
        questionListView.getSelectionModel().clearSelection();
        questionField.clear();
        tipField.clear();
        for (int i = 0; i < 4; i++) {
            optionFields[i].clear();
        }
        probSpinners[0].getValueFactory().setValue(40);
        probSpinners[1].getValueFactory().setValue(25);
        probSpinners[2].getValueFactory().setValue(20);
        probSpinners[3].getValueFactory().setValue(15);
        correctCombo.getSelectionModel().selectFirst();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}