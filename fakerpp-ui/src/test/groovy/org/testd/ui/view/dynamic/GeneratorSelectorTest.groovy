package org.testd.ui.view.dynamic

import javafx.collections.FXCollections
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.MenuItem
import javafx.stage.Stage
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.ui.model.ColProperty
import org.testd.ui.model.TableProperty
import org.testd.ui.vo.ColFamilyVO
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec

@SpringBootTest
class GeneratorSelectorTest extends ApplicationSpec {

    @Autowired
    BeanFactory beanFactory

    TableProperty.GeneratorInfoProperty testGenInfo
    GeneratorSelector gs

    @Override
    void init() throws Exception {
        FxToolkit.registerStage { new Stage() }
    }

    @Override
    void start(Stage stage) throws Exception {
        gs = beanFactory.getBean(GeneratorSelector.class)
        testGenInfo = TableProperty.GeneratorInfoProperty.defaultProperty()
        def cfVO = new ColFamilyVO(new TableProperty.ColFamilyProperty(
                FXCollections.observableArrayList("a"),
                FXCollections.observableArrayList(testGenInfo)), {colName -> new ColProperty(colName)})
        gs.init(cfVO, testGenInfo)
        stage.setScene(new Scene(gs, 600, 400))
        stage.show()
    }

    @Override
    void stop() throws Exception {
        FxToolkit.cleanupStages()
    }

    def "edit test str param"() {
        given:
        interact({
            gs.fieldInput.selectionModel.select("built-in")
            gs.generatorInput.selectionModel.select("str")
        })

        when:
        rightClickOn("#generatorInput")
        clickOn("#Edit_Params_mi")
        clickOn("#len .text-field")
        write("5")
        clickOn("#prefix .text-field")
        write("XXX")
        clickOn("#formOkButton")

        then:
        testGenInfo.field.get() == "built-in"
        testGenInfo.generator.get() == "str"
        testGenInfo.attributes["len"].get() == "105"
        testGenInfo.attributes["prefix"].get() == "XXX"
        testGenInfo.attributes["suffix"].get() == ""
    }
}
