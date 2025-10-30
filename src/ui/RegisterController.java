package ui;

import dao.UserDAO;
import models.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;

public class RegisterController {

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private TextField fullName;
    @FXML private ChoiceBox<String> roleChoice;
    @FXML private ChoiceBox<String> classChoice;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        roleChoice.getItems().addAll("STUDENT","TEACHER");
        roleChoice.setValue("STUDENT");
        classChoice.getItems().addAll("A","B","C","D");
        classChoice.setValue("A");
    }

    @FXML
    private void handleRegister() {
        String u = username.getText().trim();
        String p = password.getText().trim();
        String fn = fullName.getText().trim();
        String role = roleChoice.getValue();
        if (u.isEmpty() || p.isEmpty() || fn.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation", "All fields are required.");
            return;
        }
        try {
            // check username uniqueness
            java.util.List<models.User> existing = userDAO.listUsers();
            for (models.User exu : existing) {
                if (exu.getUsername().equalsIgnoreCase(u)) {
                    showAlert(Alert.AlertType.ERROR, "Validation", "Username already exists. Choose another.");
                    return;
                }
            }
        } catch (Exception ioe) {
            // non-fatal, proceed to attempt insert and handle DB errors
        }
        try {
            User user = new User();
            user.setUsername(u);
            user.setPassword(p);
            user.setFullName(fn);
            user.setRole(role);
            if ("STUDENT".equals(role)) {
                user.setStudentClass(classChoice.getValue());
            }
            int newId = userDAO.addUser(user);
            user.setId(newId);

            // prepare CSS URL if present
            File css = new File("resources/catppuccin-mocha.css");
            String cssUrl = null;
            if (css.exists()) cssUrl = css.toURI().toURL().toExternalForm();
            if ("TEACHER".equals(role)) {
                File fxml = new File("resources/TeacherDashboard.fxml");
                FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
                Parent root = loader.load();
                TeacherDashboardController ctrl = loader.getController();
                ctrl.setTeacher(user);
                Scene scene = new Scene(root);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl);
                // reuse the register stage as the app window
                Stage st = UIUtils.getStage(username);
                if (st != null) {
                    st.setTitle("Teacher Dashboard - " + user.getFullName());
                    st.setScene(scene);
                    st.show();
                } else {
                    Stage tmp = new Stage();
                    tmp.setTitle("Teacher Dashboard - " + user.getFullName());
                    tmp.setScene(scene);
                    tmp.show();
                }
            } else {
                File fxml = new File("resources/StudentDashboard.fxml");
                FXMLLoader loader = new FXMLLoader(fxml.toURI().toURL());
                Parent root = loader.load();
                StudentDashboardController ctrl = loader.getController();
                ctrl.setStudent(user);
                Scene scene = new Scene(root);
                if (cssUrl != null) scene.getStylesheets().add(cssUrl);
                Stage st = UIUtils.getStage(username);
                if (st != null) {
                    st.setTitle("Student Dashboard - " + user.getFullName());
                    st.setScene(scene);
                    st.show();
                } else {
                    Stage tmp = new Stage();
                    tmp.setTitle("Student Dashboard - " + user.getFullName());
                    tmp.setScene(scene);
                    tmp.show();
                }
            }

            // close register window only if a separate stage exists
            Stage stage = ui.UIUtils.getStage(username);
            if (stage != null) stage.close();
        } catch (java.sql.SQLIntegrityConstraintViolationException sicv) {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username already exists.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", ex.toString());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = UIUtils.getStage(username);
        if (stage != null) stage.close();
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
