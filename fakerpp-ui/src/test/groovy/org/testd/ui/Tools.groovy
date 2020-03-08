package org.testd.ui


import javafx.scene.Scene
import javafx.stage.Stage
import org.testd.ui.fxweaver.core.FxWeaver

class Tools {

    static void showContent(content) {
        def stage = new Stage()
        def scene = new Scene(content)
        stage.setScene(scene)
        stage.showAndWait()
    }

    static void show(Class<?> viewClass, FxWeaver fxWeaver) {
        showContent(fxWeaver.loadControl(viewClass))
    }
}
