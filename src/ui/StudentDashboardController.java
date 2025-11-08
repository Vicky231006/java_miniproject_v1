package ui;

import dao.QuizDAO;
import models.Quiz;
import models.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.File;
import java.util.List;

public class StudentDashboardController {

    @FXML private TableView<Quiz> quizzesTable;
    @FXML private TableColumn<Quiz, Integer> colId;
    @FXML private TableColumn<Quiz, String> colTitle;
    @FXML private TableColumn<Quiz, String> colDesc;
    @FXML private TableColumn<Quiz, String> colCourse;
    @FXML private TableColumn<Quiz, String> colDeadline;
    @FXML private TextField searchField;
    @FXML private Button takeQuizButton;
    @FXML private Button viewResultsButton;
    @FXML private Label studentLabel;
    @FXML private Button signOutButton;

    private User student;
    private QuizDAO quizDAO = new QuizDAO();
    private ObservableList<Quiz> quizList = FXCollections.observableArrayList();

    public void setStudent(User s) {
        this.student = s;
        studentLabel.setText("Logged in as: " + student.getFullName());
        loadQuizzes();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        colTitle.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        colDesc.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
    // Show only quiz course name (student dashboard requirement)
    colCourse.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
        data.getValue().getCourseName() != null && !data.getValue().getCourseName().isEmpty() ? data.getValue().getCourseName() : data.getValue().getTitle()
    ));
        colDeadline.setCellValueFactory(data -> {
            try {
                if (data.getValue().getDeadline() == null) return new javafx.beans.property.SimpleStringProperty("");
                java.time.LocalDateTime dl = data.getValue().getDeadline();
                if (java.time.LocalDateTime.now().isAfter(dl)) {
                    return new javafx.beans.property.SimpleStringProperty("Deadline is done");
                } else {
                    return new javafx.beans.property.SimpleStringProperty(dl.toString());
                }
            } catch (Exception ex) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
        // wire sign out button programmatically (FXML no longer references onAction)
        if (signOutButton != null) {
            signOutButton.setOnAction(e -> handleSignOut());
        }
    }

    private void loadQuizzes() {
        try {
            List<Quiz> list = quizDAO.listQuizzes();
            // filter quizzes: only those targeted to this student's stream/division or ALL
            java.util.List<Quiz> filtered = new java.util.ArrayList<>();
            for (Quiz q : list) {
                if (isAssignedToStudent(q)) filtered.add(q);
            }
            quizList.setAll(filtered);
            quizzesTable.setItems(quizList);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error loading", ex.getMessage());
        }
    }

    private boolean isAssignedToStudent(Quiz q) {
        if (student == null) return false;
        // target_stream: comma-separated or 'ALL'
        String ts = q.getTargetStream();
        String td = q.getTargetDivisions();
        if (ts == null || ts.trim().isEmpty()) ts = "ALL";
        if (td == null || td.trim().isEmpty()) td = "ALL";
        String studentStream = student.getStream();
        String studentDivision = student.getDivision();
    if (studentStream == null) studentStream = "";
    if (studentDivision == null) studentDivision = "";
    studentStream = studentStream.trim();
    studentDivision = studentDivision.trim();
        boolean streamAllowed = false;
        for (String part : ts.split(",")) {
            String p = part.trim();
            if (p.equalsIgnoreCase("ALL") || p.equalsIgnoreCase(studentStream)) { streamAllowed = true; break; }
        }
        boolean divisionAllowed = false;
        for (String part : td.split(",")) {
            String p = part.trim();
            if (p.equalsIgnoreCase("ALL") || p.equalsIgnoreCase(studentDivision)) { divisionAllowed = true; break; }
        }
        return streamAllowed && divisionAllowed;
    }

    @FXML
    private void handleTakeQuiz() {
        Quiz sel = quizzesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Select Quiz", "Please select a quiz to take.");
            return;
        }
        // ensure student is allowed and deadline not passed
        if (!isAssignedToStudent(sel)) {
            showAlert(Alert.AlertType.ERROR, "Not Allowed", "You are not eligible to take this quiz.");
            return;
        }
        if (sel.getDeadline() != null && java.time.LocalDateTime.now().isAfter(sel.getDeadline())) {
            showAlert(Alert.AlertType.ERROR, "Deadline Passed", "This quiz's deadline has passed and it cannot be attempted.");
            return;
        }
        try {
            File fxml = new File("resources/QuizAttempt.fxml");
            FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
            Parent root = loader.load();
            QuizAttemptController ctrl = loader.getController();
            ctrl.setStudentAndQuiz(student, sel);
            Stage stage = new Stage();
            stage.setTitle("Take Quiz - " + sel.getTitle());
            Scene s = new Scene(root);
            File css = new File("resources/catppuccin-mocha.css");
            if (css.exists()) s.getStylesheets().add(css.toURI().toURL().toExternalForm());
            stage.setScene(s);
            stage.showAndWait();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error opening quiz", ex.getMessage());
        }
    }

    @FXML
    private void handleViewResults() {
        try {
            File fxml = new File("resources/Results.fxml");
            FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
            Parent root = loader.load();
            ResultsController ctrl = loader.getController();
            ctrl.setStudent(student);
            Stage stage = new Stage();
            stage.setTitle("Your Results");
            Scene s = new Scene(root);
            File css = new File("resources/catppuccin-mocha.css");
            if (css.exists()) s.getStylesheets().add(css.toURI().toURL().toExternalForm());
            stage.setScene(s);
            stage.showAndWait();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            quizzesTable.setItems(quizList);
            return;
        }
        ObservableList<Quiz> filtered = FXCollections.observableArrayList();
        for (Quiz quiz : quizList) {
            if (quiz.getTitle().toLowerCase().contains(q) || (quiz.getDescription()!=null && quiz.getDescription().toLowerCase().contains(q))) {
                filtered.add(quiz);
            }
        }
        quizzesTable.setItems(filtered);
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void handleSignOut() {
        try {
            // replace current scene on same stage with login scene (safe)
            Stage st = ui.UIUtils.getStage(studentLabel);
            if (st == null) return;
            File fxml = new File("resources/Login.fxml");
            FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
            Parent root = loader.load();
            Scene scene = new Scene(root);
            File css = new File("resources/catppuccin-mocha.css");
            if (css.exists()) scene.getStylesheets().add(css.toURI().toURL().toExternalForm());
            st.setTitle("Online Quiz Management System - Login");
            st.setScene(scene);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }
}
