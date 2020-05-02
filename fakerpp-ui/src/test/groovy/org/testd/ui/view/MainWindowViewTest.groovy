package org.testd.ui.view

import com.google.common.collect.ImmutableList
import javafx.scene.Scene
import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.fakerpp.core.parser.ast.DataSourceInfo
import org.testd.fakerpp.core.parser.ast.Table
import org.testd.ui.controller.DrawBoardController
import org.testd.ui.controller.MetaController
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.model.DataSourceInfoProperty
import org.testd.ui.model.TableProperty
import org.testd.ui.view.dynamic.MyTableView
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec

@SpringBootTest
class MainWindowViewTest extends ApplicationSpec {

    @Autowired
    FxWeaver fxWeaver

    MainWindowView mainWindowView

    @Autowired
    MetaView metaView

    @Autowired
    MetaController metaController

    @Autowired
    DrawBoardController drawBoardController

    Stage testStage

    @Override
    void init() throws Exception {
        FxToolkit.registerStage { new Stage() }
    }

    @Override
    void start(Stage stage) throws Exception {
        testStage = stage
        def controllerAndView = fxWeaver.load(MainWindowView.class)
        mainWindowView = controllerAndView.getController()
        stage.setScene(new Scene(controllerAndView.getView().get(), 1000, 600))
        stage.show()
    }

    @Override
    void stop() throws Exception {
        FxToolkit.cleanupStages()
    }

    def "can not delete data source when data source is in use"() {
        given:
        def dsip = new DataSourceInfoProperty(new DataSourceInfo("aaaa",
                "", "",
                10, "", "", ""))

        def tableView = fxWeaver.loadControl(MyTableView.class)

        interact({
            tableView.initTableProperty(TableProperty.map(
                    new Table("aTable", "", 10,
                            new Table.Joins(ImmutableList.of(), ImmutableList.of()),
                            ImmutableList.of(), ImmutableList.of()),
                    dsip, 0, 0
            ))
            metaView.appendDataSource(dsip)
            drawBoardController.append(tableView)
            metaView.dataSourceTable.selectionModel.select(0)
        })

        when:
        rightClickOn("#dataSourceTable .table-row-cell")
        clickOn("#Delete_mi")

        then:
        listTargetWindows().size() == 3
        metaView.dataSourceTable.getItems().size() == 1
        metaView.dataSourceTable.getItems()[0] == dsip
        metaController.getDsInfos().size() == 1
        metaController.getDsInfos()[0] == dsip
    }
}
