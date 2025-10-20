import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        File fxml = new File("resources/Login.fxml");
        Parent root = FXMLLoader.load(fxml.toURI().toURL());
        primaryStage.setTitle("Online Quiz Management System - Login");
        primaryStage.setScene(new Scene(root, 480, 380));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
