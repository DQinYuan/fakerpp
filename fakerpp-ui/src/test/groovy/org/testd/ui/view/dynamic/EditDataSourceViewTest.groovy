package org.testd.ui.view.dynamic

import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.fakerpp.core.parser.ast.DataSourceInfo
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.model.DataSourceInfoProperty
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec

@SpringBootTest
class EditDataSourceViewTest extends ApplicationSpec {

    @Autowired
    FxWeaver fxWeaver

    EditDataSourceView editDataSourceView

    @Override
    void init() throws Exception {
        FxToolkit.registerStage { new Stage() }
    }

    @Override
    void start(Stage stage) throws Exception {
    }

    @Override
    void stop() throws Exception {
        FxToolkit.cleanupStages()
    }

    def "complete button disable when name is empty"(String name, boolean expecDisable) {
        when:
        def editDataSourceView = fxWeaver.loadControl(EditDataSourceView.class)
        editDataSourceView.init(new DataSourceInfoProperty(new DataSourceInfo(name,
                "mysql", "default",
                10, "", "", "")),
                {})

        then:
        editDataSourceView.okButton.disabledProperty().get() == expecDisable

        where:
        name | expecDisable
        ""   | true
        "a"  | false
    }

}
