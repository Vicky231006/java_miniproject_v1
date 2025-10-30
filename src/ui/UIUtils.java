package ui;

import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.Window;

public class UIUtils {
    public static Stage getStage(Node node) {
        if (node == null) return null;
        Scene s = node.getScene();
        if (s == null) return null;
        Window w = s.getWindow();
        if (w instanceof Stage) return (Stage) w;
        return null;
    }
}
