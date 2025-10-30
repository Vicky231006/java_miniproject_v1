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
        // wire sign out button programmatically (FXML no longer references onAction)
        if (signOutButton != null) {
            signOutButton.setOnAction(e -> handleSignOut());
        }
    }

    private void loadQuizzes() {
        try {
            List<Quiz> list = quizDAO.listQuizzes();
            quizList.setAll(list);
            quizzesTable.setItems(quizList);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error loading", ex.getMessage());
        }
    }

    @FXML
    private void handleTakeQuiz() {
        Quiz sel = quizzesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Select Quiz", "Please select a quiz to take.");
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
