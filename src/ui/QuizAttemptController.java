package ui;

import dao.QuestionDAO;
import dao.ResultDAO;
import models.Question;
import models.Result;
import models.Quiz;
import models.User;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class QuizAttemptController {

    @FXML private Label quizTitleLabel;
    @FXML private VBox questionsContainer;
    @FXML private Button submitBtn;

    private User student;
    private Quiz quiz;
    private QuestionDAO questionDAO = new QuestionDAO();
    private ResultDAO resultDAO = new ResultDAO();

    private List<Question> questions = new ArrayList<>();
    private Map<Integer, ToggleGroup> tgMap = new HashMap<>();

    public void setStudentAndQuiz(User s, Quiz q) {
        this.student = s;
        this.quiz = q;
        loadQuestions();
    }

    private void loadQuestions() {
        try {
            quizTitleLabel.setText(quiz.getTitle());
            questions = questionDAO.listQuestionsByQuiz(quiz.getId());
            questionsContainer.getChildren().clear();
            for (Question qt : questions) {
                VBox box = new VBox(4);
                Label qLabel = new Label(qt.getQuestionText());
                RadioButton ra = new RadioButton("A. " + qt.getOptionA());
                RadioButton rb = new RadioButton("B. " + qt.getOptionB());
                RadioButton rc = new RadioButton("C. " + qt.getOptionC());
                RadioButton rd = new RadioButton("D. " + qt.getOptionD());
                ToggleGroup tg = new ToggleGroup();
                ra.setToggleGroup(tg);
                rb.setToggleGroup(tg);
                rc.setToggleGroup(tg);
                rd.setToggleGroup(tg);
                tgMap.put(qt.getId(), tg);
                box.getChildren().addAll(qLabel, ra, rb, rc, rd);
                // use .card style class for consistent theming
                box.getStyleClass().add("card");
                questionsContainer.getChildren().add(box);
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error loading questions", ex.getMessage());
        }
    }

    @FXML
    private void handleSubmit() {
        try {
            if (questions.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No Questions", "No questions to submit.");
                return;
            }
            int total = questions.size();
            int correct = 0;
            StringBuilder answers = new StringBuilder();
            for (Question q : questions) {
                ToggleGroup tg = tgMap.get(q.getId());
                String selected = null;
                if (tg != null && tg.getSelectedToggle() != null) {
                    RadioButton sel = (RadioButton) tg.getSelectedToggle();
                    String txt = sel.getText();
                    selected = txt.substring(0,1);
                } else {
                    selected = "-";
                }
                answers.append("Q").append(q.getId()).append(":").append(selected).append(";");
                if (selected != null && selected.length() > 0 && selected.charAt(0) == q.getCorrectOption()) {
                    correct++;
                }
            }
            double score = ((double) correct / total) * 100.0;
            Result r = new Result();
            r.setStudentId(student.getId());
            r.setQuizId(quiz.getId());
            r.setScore(score);
            r.setTotalQuestions(total);
            r.setAnswers(answers.toString());
            resultDAO.addResult(r);

            showAlert(Alert.AlertType.INFORMATION, "Submitted", "Score: " + String.format("%.2f", score) + "% (" + correct + "/" + total + ")");
            Stage stage = UIUtils.getStage(submitBtn);
            if (stage != null) stage.close();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error submitting", ex.getMessage());
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
