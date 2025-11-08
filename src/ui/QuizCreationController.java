package ui;

import dao.QuestionDAO;
import dao.QuizDAO;
import models.Question;
import models.Quiz;
import models.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class QuizCreationController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField courseField;
    @FXML private TextField deadlineField; // ISO-like input
    @FXML private TextField timeLimitField;
    @FXML private ChoiceBox<String> targetStreamChoice;
    @FXML private TextField targetDivisionsField;
    @FXML private TextField qText;
    @FXML private TextField optA, optB, optC, optD;
    @FXML private ChoiceBox<String> correctChoice;
    @FXML private Button addQuestionBtn;
    @FXML private ListView<Question> questionsListView;
    @FXML private Button saveQuizBtn;

    private User teacher;
    private QuizDAO quizDAO = new QuizDAO();
    private QuestionDAO questionDAO = new QuestionDAO();
    private Quiz editingQuiz = null;
    private ObservableList<Question> questions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        correctChoice.getItems().addAll("A", "B", "C", "D");
        correctChoice.setValue("A");
        questionsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getQuestionText() + " (Correct: " + item.getCorrectOption() + ")");
                    // style the cell for dark background
                    setStyle("-fx-background-color: #0f0e16; -fx-text-fill: #c6c7d0;");
                }
            }
        });
        questionsListView.setItems(questions);
        // populate target stream choices
        if (targetStreamChoice != null) {
            targetStreamChoice.getItems().addAll("ALL", "Computer Engg", "Mech Engg", "Comp Sci Engg", "ECS");
            targetStreamChoice.setValue("ALL");
        }
        // allow deleting selected question via context menu or DEL key
        ContextMenu cm = new ContextMenu();
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(e -> {
            Question sel = questionsListView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                // if it already exists in DB (has id > 0) delete it there too
                if (sel.getId() > 0) {
                    try {
        // Initialize target stream choices if not already done
        if (targetStreamChoice != null && targetStreamChoice.getItems().isEmpty()) {
            targetStreamChoice.setItems(FXCollections.observableArrayList("ALL", "CSE", "IT", "MECH", "CIVIL", "ELEC"));
        }
        
        // Add validation for time limit field
        if (timeLimitField != null) {
            timeLimitField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    timeLimitField.setText(newVal.replaceAll("[^\\d]", ""));
                }
            });
            timeLimitField.setPromptText("Enter time limit in minutes");
        }
        
        if (deadlineField != null) {
            deadlineField.setPromptText("YYYY-MM-DDTHH:MM (e.g. 2025-12-31T23:59)");
        }
                        questionDAO.deleteQuestion(sel.getId());
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                    }
                }
                questions.remove(sel);
            }
        });
        cm.getItems().add(delete);
        questionsListView.setContextMenu(cm);
        questionsListView.setOnKeyPressed(ev -> {
            switch (ev.getCode()) {
                case DELETE -> {
                    Question sel = questionsListView.getSelectionModel().getSelectedItem();
                    if (sel != null) {
                        if (sel.getId() > 0) {
                            try {
                                questionDAO.deleteQuestion(sel.getId());
                            } catch (Exception ex) {
                                showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                            }
                        }
                        questions.remove(sel);
                    }
                }
                default -> {}
            }
        });
    }

    public void setTeacher(User t) { this.teacher = t; }

    public void loadQuizForEdit(Quiz q) {
        try {
            this.editingQuiz = q;
            titleField.setText(q.getTitle());
            descriptionArea.setText(q.getDescription());
            List<Question> qlist = questionDAO.listQuestionsByQuiz(q.getId());
            questions.setAll(qlist);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    @FXML
    private void handleAddQuestion() {
        String qt = qText.getText().trim();
        String a = optA.getText().trim();
        String b = optB.getText().trim();
        String c = optC.getText().trim();
        String d = optD.getText().trim();
        String corr = correctChoice.getValue();
        if (qt.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "All question fields and options must be filled.");
            return;
        }
        Question q = new Question();
        q.setQuestionText(qt);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectOption(corr.charAt(0));
        questions.add(q);
        qText.clear(); optA.clear(); optB.clear(); optC.clear(); optD.clear();
    }

    @FXML
    private void handleSaveQuiz() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Quiz title is required.");
            return;
        }
        try {
            int quizId;
            if (editingQuiz == null) {
                Quiz q = new Quiz();
                q.setTitle(title);
                q.setDescription(descriptionArea.getText());
                q.setTeacherId(teacher.getId());
                // optional metadata
                q.setCourseName(courseField != null ? courseField.getText().trim() : null);
                if (deadlineField != null && !deadlineField.getText().trim().isEmpty()) {
                    try {
                        q.setDeadline(java.time.LocalDateTime.parse(deadlineField.getText().trim()));
                    } catch (Exception pe) {
                        showAlert(Alert.AlertType.ERROR, "Validation", "Invalid deadline format. Use YYYY-MM-DDTHH:MM");
                        return;
                    }
                }
                if (timeLimitField != null && !timeLimitField.getText().trim().isEmpty()) {
                    try {
                        q.setTimeLimit(Integer.parseInt(timeLimitField.getText().trim()));
                    } catch (NumberFormatException nfe) {
                        showAlert(Alert.AlertType.ERROR, "Validation", "Time limit must be an integer (minutes).");
                        return;
                    }
                }
                if (targetStreamChoice != null) q.setTargetStream(targetStreamChoice.getValue());
                if (targetDivisionsField != null) q.setTargetDivisions(targetDivisionsField.getText().trim());
                quizId = quizDAO.addQuiz(q);
            } else {
                editingQuiz.setTitle(title);
                editingQuiz.setDescription(descriptionArea.getText());
                // update optional fields on edit as well
                editingQuiz.setCourseName(courseField != null ? courseField.getText().trim() : editingQuiz.getCourseName());
                if (deadlineField != null && !deadlineField.getText().trim().isEmpty()) {
                    try {
                        editingQuiz.setDeadline(java.time.LocalDateTime.parse(deadlineField.getText().trim()));
                    } catch (Exception pe) {
                        showAlert(Alert.AlertType.ERROR, "Validation", "Invalid deadline format. Use YYYY-MM-DDTHH:MM");
                        return;
                    }
                }
                if (timeLimitField != null && !timeLimitField.getText().trim().isEmpty()) {
                    try {
                        editingQuiz.setTimeLimit(Integer.parseInt(timeLimitField.getText().trim()));
                    } catch (NumberFormatException nfe) {
                        showAlert(Alert.AlertType.ERROR, "Validation", "Time limit must be an integer (minutes).");
                        return;
                    }
                }
                if (targetStreamChoice != null) editingQuiz.setTargetStream(targetStreamChoice.getValue());
                if (targetDivisionsField != null) editingQuiz.setTargetDivisions(targetDivisionsField.getText().trim());
                quizDAO.updateQuiz(editingQuiz);
                quizId = editingQuiz.getId();
            }

            for (Question question : questions) {
                question.setQuizId(quizId);
                questionDAO.addQuestion(question);
            }

            showAlert(Alert.AlertType.INFORMATION, "Saved", "Quiz saved successfully.");
            Stage stage = UIUtils.getStage(saveQuizBtn);
            if (stage != null) stage.close();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error saving", ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
