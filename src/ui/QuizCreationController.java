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
                }
            }
        });
        questionsListView.setItems(questions);
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
                quizId = quizDAO.addQuiz(q);
            } else {
                editingQuiz.setTitle(title);
                editingQuiz.setDescription(descriptionArea.getText());
                quizDAO.updateQuiz(editingQuiz);
                quizId = editingQuiz.getId();
            }

            for (Question question : questions) {
                question.setQuizId(quizId);
                questionDAO.addQuestion(question);
            }

            showAlert(Alert.AlertType.INFORMATION, "Saved", "Quiz saved successfully.");
            Stage stage = (Stage) saveQuizBtn.getScene().getWindow();
            stage.close();
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
