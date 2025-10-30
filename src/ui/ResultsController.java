package ui;

import dao.ResultDAO;
import dao.UserDAO;
import models.Result;
import models.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ResultsController {

    @FXML private TableView<Result> resultsTable;
    @FXML private TableColumn<Result, Integer> colId;
    @FXML private TableColumn<Result, Integer> colQuizId;
    @FXML private TableColumn<Result, Double> colScore;
    @FXML private TableColumn<Result, Integer> colTotal;
    @FXML private TableColumn<Result, String> colAnswers;
    @FXML private TableColumn<Result, String> colStudent;
    @FXML private TableColumn<Result, String> colStudentClass;

    private User student;
    private ResultDAO resultDAO = new ResultDAO();
    private UserDAO userDAO = new UserDAO();
    private ObservableList<Result> results = FXCollections.observableArrayList();

    public void setStudent(User s) {
        this.student = s;
        loadResults();
    }

    // allow programmatic loading of results for a specific quiz (teacher view)
    public void loadResultsByQuiz(int quizId) {
        try {
            List<Result> list = resultDAO.listResultsByQuiz(quizId);
            results.setAll(list);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        colQuizId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuizId()));
        colScore.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getScore()));
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotalQuestions()));
        colAnswers.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAnswers()));
        colStudent.setCellValueFactory(data -> {
            try {
                User u = userDAO.getById(data.getValue().getStudentId());
                return new javafx.beans.property.SimpleStringProperty(u != null ? u.getFullName() : "(unknown)");
            } catch (Exception ex) {
                return new javafx.beans.property.SimpleStringProperty("(error)");
            }
        });
        colStudentClass.setCellValueFactory(data -> {
            try {
                User u = userDAO.getById(data.getValue().getStudentId());
                return new javafx.beans.property.SimpleStringProperty(u != null ? (u.getStudentClass()!=null ? u.getStudentClass() : "") : "");
            } catch (Exception ex) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
        resultsTable.setItems(results);
    }

    private void loadResults() {
        try {
            List<Result> list = resultDAO.listResultsByStudent(student.getId());
            results.setAll(list);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
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
