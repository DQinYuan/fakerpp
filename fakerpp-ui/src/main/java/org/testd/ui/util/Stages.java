package org.testd.ui.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Stages {

    public static void newSceneInChild(Parent content, Window parentWindow) {
        newSceneInChild(content, parentWindow, "");
    }

    public static void newSceneInChild(Parent content, Window parentWindow, String title) {
        Stage child = child(parentWindow);
        child.setTitle(title);
        child.setScene(new Scene(content));
        child.showAndWait();
    }

    public static Stage child(Window parentWindow) {
        Stage s = new Stage();
        s.initOwner(parentWindow);
        s.initModality(Modality.WINDOW_MODAL);
        return s;
    }

    public static void closeWindow(Window window) {
        ((Stage)window).close();
    }

}
