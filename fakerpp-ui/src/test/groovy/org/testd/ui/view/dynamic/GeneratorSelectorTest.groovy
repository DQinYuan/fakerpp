package org.testd.ui.view.dynamic

import javafx.scene.Scene
import javafx.stage.Stage
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec

class GeneratorSelectorTest extends ApplicationSpec {

    @Override
    void init() throws Exception {
        FxToolkit.registerStage { new Stage() }
    }


    @Override
    void start(Stage stage) throws Exception {
        def gs = new GeneratorSelector()
        gs.postConstruct()
        stage.setScene(new Scene(gs, 600, 400))
        stage.showAndWait()
    }

    @Override
    void stop() throws Exception {
        FxToolkit.cleanupStages()
    }

/*    def "test show"() {
        expect:
        Thread.sleep(10000)
    }*/
}
