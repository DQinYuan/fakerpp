package org.testd.ui.view.dynamic

import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.model.ColProperty
import org.testd.ui.model.TableProperty
import org.testd.ui.vo.ColFamilyVO
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec

@SpringBootTest
class ColFamilyViewTest extends ApplicationSpec {

    @Autowired
    FxWeaver fxWeaver

    TableProperty.ColFamilyProperty colFamilyProperty


    @Override
    void init() throws Exception {
        FxToolkit.registerStage { new Stage() }
    }

    @Override
    void start(Stage stage) throws Exception {
        colFamilyProperty = new TableProperty.ColFamilyProperty(
                FXCollections.observableArrayList("a"),
                FXCollections.observableArrayList()
        )
        def colFamilyVO = new ColFamilyVO(colFamilyProperty, {colName -> new ColProperty(colName)})
        def colFamilyView = fxWeaver.loadControl(ColFamilyView.class)
        def mockTable = fxWeaver.loadControl(MyTableView.class)
        mockTable.initTableProperty(new TableProperty("ColFamilyViewTest"))
        colFamilyView.initFromTableAndColFamilyVO(
                mockTable, colFamilyVO)
        stage.setScene(new Scene(colFamilyView, 600, 400))
        stage.show()
    }

    def "generator compose"() {
        expect:
        assert colFamilyProperty.generatorInfos.size() == 1
        rightClickOn("#generatorInput")
        clickOn("#Compose_More_Generator_mi")
        assert colFamilyProperty.generatorInfos.size() == 2
    }

}
