package ui;

import dao.QuestionDAO;
import dao.ResultDAO;
import models.Question;
import models.Result;
import models.Quiz;
import models.User;

import java.util.*;
import java.time.LocalDateTime;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class QuizAttemptController {

    @FXML private Label quizTitleLabel;
    @FXML private VBox questionsContainer;
    @FXML private Button submitBtn;
    @FXML private Label timerLabel;

    private User student;
    private Quiz quiz;
    private QuestionDAO questionDAO = new QuestionDAO();
    private ResultDAO resultDAO = new ResultDAO();

    private List<Question> questions = new ArrayList<>();
    private Map<Integer, ToggleGroup> tgMap = new HashMap<>();
    private LocalDateTime quizStartTime = null;
    private Timeline timerTimeline = null;

    public void setStudentAndQuiz(User s, Quiz q) {
        this.student = s;
        this.quiz = q;
        // enforce that student belongs to quiz target stream/division
        try {
            String ts = q.getTargetStream();
            String td = q.getTargetDivisions();
            if (ts == null || ts.trim().isEmpty()) ts = "ALL";
            if (td == null || td.trim().isEmpty()) td = "ALL";
            String studentStream = s.getStream() == null ? "" : s.getStream();
            String studentDiv = s.getDivision() == null ? "" : s.getDivision();
            studentStream = studentStream.trim();
            studentDiv = studentDiv.trim();
            quizStartTime = LocalDateTime.now(); // Set start time immediately
            boolean streamAllowed = false;
            for (String part : ts.split(",")) {
                String p = part.trim();
                if (p.equalsIgnoreCase("ALL") || p.equalsIgnoreCase(studentStream)) { streamAllowed = true; break; }
            }
            boolean divisionAllowed = false;
            for (String part : td.split(",")) {
                String p = part.trim();
                if (p.equalsIgnoreCase("ALL") || p.equalsIgnoreCase(studentDiv)) { divisionAllowed = true; break; }
            }
            if (!streamAllowed || !divisionAllowed) {
                showAlert(Alert.AlertType.ERROR, "Not Allowed", "You are not eligible to attempt this quiz.");
                if (submitBtn != null) submitBtn.setDisable(true);
                return;
            }
        } catch (Exception ex) {
            // ignore and continue to deadline check
        }
        // enforce deadline
        try {
            if (quiz.getDeadline() != null) {
                if (LocalDateTime.now().isAfter(quiz.getDeadline())) {
                    showAlert(Alert.AlertType.ERROR, "Unavailable", "This quiz has passed its deadline and cannot be attempted.");
                    if (submitBtn != null) submitBtn.setDisable(true);
                    return;
                }
            }
        } catch (Exception ex) {
            // ignore and attempt to load
        }
        loadQuestions();
        // start timer UI if applicable
        startTimerIfNeeded();
    }

    private void loadQuestions() {
        try {
            quizTitleLabel.setText(quiz.getTitle());
            questions = questionDAO.listQuestionsByQuiz(quiz.getId());
            questionsContainer.getChildren().clear();
            // record start time for time-limit enforcement
            quizStartTime = LocalDateTime.now();
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
            // update the timer label immediately
            updateTimerLabel();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error loading questions", ex.getMessage());
        }
    }

    @FXML
    private void handleSubmit() {
        try {
            // enforce time limit if set
            if (quiz.getTimeLimit() != null && quizStartTime != null) {
                long elapsedMinutes = java.time.Duration.between(quizStartTime, LocalDateTime.now()).toMinutes();
                if (elapsedMinutes > quiz.getTimeLimit()) {
                    showAlert(Alert.AlertType.ERROR, "Time Exceeded", "Time limit for this quiz has been exceeded. Submission blocked.");
                    return;
                }
            }
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
            // stop timer when leaving
            stopTimer();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error submitting", ex.getMessage());
        }
    }

    private void startTimerIfNeeded() {
        stopTimer(); // Stop any existing timer
        
        if (quiz == null || quiz.getTimeLimit() == null) {
            if (timerLabel != null) timerLabel.setText("");
            return;
        }
        
        // Initialize timer label immediately
        updateTimerLabel();
        
        // Schedule a Timeline to update every second
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            updateTimerLabel();
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();
    }

    private void stopTimer() {
        if (timerTimeline != null) {
            timerTimeline.stop();
            timerTimeline = null;
        }
    }

    private void updateTimerLabel() {
        if (timerLabel == null) return;
        if (quiz == null || quiz.getTimeLimit() == null || quizStartTime == null) {
            timerLabel.setText("");
            return;
        }
            Platform.runLater(() -> {
                long elapsedSeconds = java.time.Duration.between(quizStartTime, LocalDateTime.now()).getSeconds();
                long totalSeconds = quiz.getTimeLimit() * 60L;
                long remaining = totalSeconds - elapsedSeconds;
            
                if (remaining <= 0) {
                    timerLabel.setText("Time's Up!");
                    timerLabel.setStyle("-fx-text-fill: red;");
                    // Auto-submit when time is up
                    if (submitBtn != null && !submitBtn.isDisabled()) {
                        submitBtn.setDisable(true);
                        handleSubmit();
                    }
                    stopTimer();
                    showAlert(Alert.AlertType.WARNING, "Time's Up!", "Your time has expired. The quiz will be submitted automatically.");
                    return;
                }
            
                long mins = remaining / 60;
                long secs = remaining % 60;
                timerLabel.setText(String.format("Time remaining: %02d:%02d", mins, secs));
            
                // Make timer red when less than 5 minutes remain
                if (remaining <= 300) {
                    timerLabel.setStyle("-fx-text-fill: red;");
                } else {
                    timerLabel.setStyle("");
                }
            });
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
