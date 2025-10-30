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
        Scene scene = new Scene(root, 480, 380);
        // apply global stylesheet if present in resources/
        File css = new File("resources/catppuccin-mocha.css");
        if (css.exists()) {
            scene.getStylesheets().add(css.toURI().toURL().toExternalForm());
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
