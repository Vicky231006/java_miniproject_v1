package ui;

import dao.UserDAO;
import models.User;
import exceptions.InvalidLoginException;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.File;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private ChoiceBox<String> roleChoice;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        roleChoice.getItems().addAll("STUDENT", "TEACHER");
        roleChoice.setValue("STUDENT");
        registerButton.setOnAction(e -> handleOpenRegister());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleChoice.getValue();
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Username and password required.");
            return;
        }
        try {
            User user = userDAO.login(username, password);
            if (!user.getRole().equals(role)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Role", "Logged in user role doesn't match selected role.");
                return;
            }
            // Navigate based on role
            if ("TEACHER".equals(role)) {
                openTeacherDashboard(user);
            } else {
                openStudentDashboard(user);
            }
            // keep the same stage (dashboard replaces login scene)
            // (handled in openTeacherDashboard/openStudentDashboard)
        } catch (InvalidLoginException ex) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", ex.getMessage());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }

    private void openTeacherDashboard(User teacher) throws Exception {
        File fxml = new File("resources/TeacherDashboard.fxml");
        FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
        Parent root = loader.load();
        TeacherDashboardController ctrl = loader.getController();
        ctrl.setTeacher(teacher);
        Scene scene = new Scene(root);
        File css = new File("resources/catppuccin-mocha.css");
        if (css.exists()) scene.getStylesheets().add(css.toURI().toURL().toExternalForm());
        // reuse the same stage (replace login scene) if possible
        Stage stage = UIUtils.getStage(loginButton);
        if (stage != null) {
            stage.setTitle("Teacher Dashboard - " + teacher.getFullName());
            stage.setScene(scene);
        } else {
            // fallback to opening a new window
            stage = new Stage();
            stage.setTitle("Teacher Dashboard - " + teacher.getFullName());
            stage.setScene(scene);
            stage.show();
        }
    }

    private void openStudentDashboard(User student) throws Exception {
        File fxml = new File("resources/StudentDashboard.fxml");
        FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
        Parent root = loader.load();
        StudentDashboardController ctrl = loader.getController();
        ctrl.setStudent(student);
        Scene scene = new Scene(root);
        File css = new File("resources/catppuccin-mocha.css");
        if (css.exists()) scene.getStylesheets().add(css.toURI().toURL().toExternalForm());
        // reuse the same stage (replace login scene) if possible
        Stage stage = UIUtils.getStage(loginButton);
        if (stage != null) {
            stage.setTitle("Student Dashboard - " + student.getFullName());
            stage.setScene(scene);
        } else {
            stage = new Stage();
            stage.setTitle("Student Dashboard - " + student.getFullName());
            stage.setScene(scene);
            stage.show();
        }
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private Button registerButton;

    @FXML
    private void handleOpenRegister() {
        try {
            File fxml = new File("resources/Register.fxml");
            FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
            Parent root = loader.load();
            // apply stylesheet
            File css = new File("resources/catppuccin-mocha.css");
            if (css.exists()) root.getStylesheets().add(css.toURI().toURL().toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
        }
    }
}
