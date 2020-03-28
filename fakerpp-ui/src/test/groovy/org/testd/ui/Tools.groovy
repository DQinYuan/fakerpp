package org.testd.ui


import javafx.scene.Scene
import javafx.stage.Stage
import org.testd.ui.fxweaver.core.FxWeaver
import org.testfx.framework.spock.ApplicationSpec

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

    static void newColFamilies(ApplicationSpec spec, String input) {
        spec.clickOn("#newColFamily")
        spec.clickOn("#newCols")
        spec.write(input)
        spec.clickOn("#okButton")
    }

    static void newColFamilies(ApplicationSpec spec, String tableId, String input) {
        spec.clickOn("#" + tableId + " #newColFamily")
        spec.clickOn("#newCols")
        spec.write(input)
        spec.clickOn("#okButton")
    }
}
