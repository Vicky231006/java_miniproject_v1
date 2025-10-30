package ui;

import dao.QuizDAO;
import models.Quiz;
import models.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class TeacherDashboardController {

    @FXML private TableView<Quiz> quizzesTable;
    @FXML private TableColumn<Quiz, Integer> colId;
    @FXML private TableColumn<Quiz, String> colTitle;
    @FXML private TableColumn<Quiz, String> colDesc;
    @FXML private TextField searchField;
    @FXML private Button createQuizButton;
    @FXML private Button editQuizButton;
    @FXML private Button deleteQuizButton;
    @FXML private Button viewResultsButton;
    @FXML private Label teacherLabel;

    private User teacher;
    private QuizDAO quizDAO = new QuizDAO();
    private ObservableList<Quiz> quizList = FXCollections.observableArrayList();

    public void setTeacher(User t) {
        this.teacher = t;
        teacherLabel.setText("Logged in as: " + teacher.getFullName());
        loadQuizzes();
    }

    @FXML
    public void initialize() {
        // make columns stretch to fill the available width (avoid a blank trailing column)
        if (quizzesTable != null) {
            quizzesTable.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);
        }
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        colTitle.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        colDesc.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        // make description column take remaining width so no blank trailing column appears
        if (quizzesTable != null && colDesc != null && colId != null && colTitle != null) {
            colDesc.prefWidthProperty().bind(quizzesTable.widthProperty().subtract(colId.widthProperty()).subtract(colTitle.widthProperty()).subtract(4));
        }
        // wire sign out button action (FXML no longer references handler directly)
        if (signOutButton != null) {
            signOutButton.setOnAction(e -> handleSignOut());
        }
        if (viewResultsButton != null) {
            viewResultsButton.setOnAction(e -> handleViewResults());
        }
    }

    private void loadQuizzes() {
        try {
            List<Quiz> list = quizDAO.listQuizzesByTeacher(teacher.getId());
            quizList.setAll(list);
            quizzesTable.setItems(quizList);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error loading quizzes", ex.getMessage());
        }
    }

    @FXML
    private void handleCreateQuiz() {
        try {
            File fxml = new File("resources/QuizCreation.fxml");
            FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
            Parent root = loader.load();
            QuizCreationController ctrl = loader.getController();
            ctrl.setTeacher(teacher);
            Stage stage = new Stage();
            stage.setTitle("Create Quiz");
            Scene scene = new Scene(root);
            File css = new File("resources/catppuccin-mocha.css");
            if (css.exists()) scene.getStylesheets().add(css.toURI().toURL().toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            loadQuizzes();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    @FXML
    private void handleEditQuiz() {
        Quiz sel = quizzesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Choose Quiz", "Please select a quiz to edit.");
            return;
        }
        try {
            File fxml = new File("resources/QuizCreation.fxml");
            FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
            Parent root = loader.load();
            QuizCreationController ctrl = loader.getController();
            ctrl.setTeacher(teacher);
            ctrl.loadQuizForEdit(sel);
            Stage stage = new Stage();
            stage.setTitle("Edit Quiz");
            Scene scene = new Scene(root);
            File css = new File("resources/catppuccin-mocha.css");
            if (css.exists()) scene.getStylesheets().add(css.toURI().toURL().toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            loadQuizzes();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    @FXML
    private void handleDeleteQuiz() {
        Quiz sel = quizzesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Choose Quiz", "Please select a quiz to delete.");
            return;
        }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Delete quiz '" + sel.getTitle() + "'?", ButtonType.YES, ButtonType.NO);
        conf.setHeaderText(null);
        conf.showAndWait();
        if (conf.getResult() == ButtonType.YES) {
            try {
                boolean ok = quizDAO.deleteQuiz(sel.getId());
                if (ok) {
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Quiz deleted.");
                    loadQuizzes();
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error deleting", ex.getMessage());
            }
        }
    }

    @FXML
    private void handleViewResults() {
        Quiz sel = quizzesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Choose Quiz", "Please select a quiz to view results.");
            return;
        }
        try {
            File fxml = new File("resources/Results.fxml");
            FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
            Parent root = loader.load();
            // ResultsController currently expects a student; we'll provide a filtered view by quiz
            ResultsController ctrl = loader.getController();
            // Use a small hack: setStudent(null) and then call a new method to load results by quiz. We'll add that method next.
            ctrl.loadResultsByQuiz(sel.getId());
            Stage stage = new Stage();
            stage.setTitle("Results for: " + sel.getTitle());
            Scene scene = new Scene(root);
            File css = new File("resources/catppuccin-mocha.css");
            if (css.exists()) scene.getStylesheets().add(css.toURI().toURL().toExternalForm());
            stage.setScene(scene);
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
    private Button signOutButton;

    @FXML
    private void handleSignOut() {
        try {
            // replace current scene on the same stage with the login scene (use safe getter)
            Stage st = ui.UIUtils.getStage(teacherLabel);
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
