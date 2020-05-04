package org.testd.ui.view

import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.stereotype.Component
import org.springframework.test.annotation.DirtiesContext
import org.testd.ui.Tools
import org.testd.ui.controller.DrawBoardController
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.model.TableProperty
import org.testd.ui.view.dynamic.ConnectPolyLine
import org.testd.ui.view.dynamic.JoinView
import org.testd.ui.view.dynamic.MyTableView
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec

@SpringBootTest(classes = DrawBoardViewTestConfig.class)
class DrawBoardViewTest extends ApplicationSpec {

    @TestConfiguration
    @ComponentScan(excludeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            value = [MainWindowView.class, MetaView.class]))
    @Component
    static class DrawBoardViewTestConfig {
    }

    @Autowired
    FxWeaver fxWeaver

    @Autowired
    DrawBoardView drawBoardView

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

        stage.setScene(new Scene(drawBoardView, 1000, 600))
        stage.show()
    }

    @Override
    void stop() throws Exception {
        // clean dirty test data
        drawBoardController.clear()
        // prevent drawBoardView already set as root of another scene
        testStage.getScene().setRoot(new StackPane())
        FxToolkit.cleanupStages()
    }

    def initTable(String tableName, int x = 10, int y = 10, String... cfs) {
        def table = fxWeaver.loadControl(MyTableView.class)
        table.initTableProperty(new TableProperty(tableName))
        table.setTranslateX(x)
        table.setTranslateY(y)

        interact({
            drawBoardController.append(table)
            table.setId(tableName)
        })

        cfs.each { Tools.newColFamilies(this, tableName, it) }

        return table
    }

    def initTablePair() {
        return [initTable("Table1", 10, 10, """aaa
bbb
ccc
""", """
ll
ee
"""), initTable("Table2", 350, 150, """
ddd
eee
fff
""")]
    }

    def "test cascade delete col with join"() {
        given:
        MyTableView t1
        MyTableView t2
        (t1, t2) = initTablePair()

        when:
        clickOn("#Table1 #newConnection")
        interact({
            lookup("#targetInput").queryAs(ComboBox.class)
                    .selectionModel.select(t2.tableProperty())
        })
        lookup(".check-box").queryAll().each { clickOn(it) }
        interact({
            lookup(".text-field").queryAllAs(TextField.class).each {
                if (it.getText() == "bbb") {
                    it.setText("xxb")
                }
            }
        })
        clickOn("#okButton")

        then:
        // check init state
        def sendViews = t1.getColFamilies(JoinView.class, { true })
        sendViews.size() == 1
        sendViews[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "bbb", "ccc", "ll", "ee"] as Set
        def recvViews = t2.getColFamilies(JoinView.class, { true })
        recvViews.size() == 1
        recvViews[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "xxb", "ccc", "ll", "ee"] as Set

        // check sync property
        t2.tableProperty().getJoins().leftJoins.size() == 1
        t2.tableProperty().getJoins().rightJoins.size() == 0
        t2.tableProperty().getJoins().leftJoins[0].depend.get() == "Table1"
        t2.tableProperty().getJoins().leftJoins[0].map == [
                "aaa":"aaa",
                "bbb":"xxb",
                "ccc":"ccc",
                "ll":"ll",
                "ee":"ee"
        ]

        and:
        // delete one col
        rightClickOn(t1.colFamiliesInput.children[0].colsInput)
        clickOn("#edit_cols_mi")
        clickOn("#newCols")
        interact({
            lookup("#newCols").queryAs(TextArea.class)
                    .setText("""
aaa
bbb
""")
        })
        clickOn("#okButton")

        then:
        // check cascade after delete one col
        sendViews[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "bbb", "ll", "ee"] as Set
        recvViews[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "xxb", "ll", "ee"] as Set
        t2.tableProperty().getJoins().leftJoins[0].map == [
                "aaa":"aaa",
                "bbb":"xxb",
                "ll":"ll",
                "ee":"ee"
        ]

        and:
        // delete entire col family
        rightClickOn(t1.colFamiliesInput.children[1].colsInput)
        clickOn("#delete_cols_mi")

        then:
        // check cascade after delete col family
        sendViews[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "bbb"] as Set
        recvViews[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "xxb"] as Set
        t2.tableProperty().getJoins().leftJoins[0].map == [
                "aaa":"aaa",
                "bbb":"xxb",
        ]

        and:
        // change join type
        rightClickOn(t2.colFamiliesInput.children[1].partInCols)
        clickOn("#edit_connection_mi")
        clickOn("#rightRadio")
        clickOn("#okButton")

        then:
        t2.tableProperty().getJoins().leftJoins.size() == 0
        t2.tableProperty().getJoins().rightJoins.size() == 1
        t2.tableProperty().getJoins().rightJoins[0].map == [
                "aaa":"aaa",
                "bbb":"xxb",
        ]
    }

    def "not append table when user simply close table conf view after new table"() {
        given:
        rightClickOn(drawBoardView)
        clickOn("#New_Table_mi")

        when:
        // close new table dialog
        interact({((Stage)listWindows()[1]).close()})

        then:
        drawBoardController.tables().size() == 0
    }

    def "cascade delete connection view when delete source table"() {
        given:
        MyTableView t1
        MyTableView t2
        (t1, t2) = initTablePair()

        // connection from t1 to t2
        clickOn("#Table1 #newConnection")
        interact({
            lookup("#targetInput").queryAs(ComboBox.class)
                    .selectionModel.select(t2.tableProperty())
        })
        lookup(".check-box").queryAll().each { clickOn(it) }
        clickOn("#okButton")

        when:
        // delete table1
        rightClickOn("#Table1 #tableNameLabel")
        clickOn("#deleteTableMenu")

        then:
        drawBoardController.elements.find { ConnectPolyLine.class.isAssignableFrom(it.getClass())} == null
        drawBoardController.tables().size() == 1
        drawBoardController.tables()[0].is(t2.tableProperty())
    }

    def "cascade delete connection view when delete target table"() {
        given:
        MyTableView t1
        MyTableView t2
        (t1, t2) = initTablePair()

        // connection from t1 to t2
        clickOn("#Table1 #newConnection")
        interact({
            lookup("#targetInput").queryAs(ComboBox.class)
                    .selectionModel.select(t2.tableProperty())
        })
        lookup(".check-box").queryAll().each { clickOn(it) }
        clickOn("#okButton")

        when:
        // delete table2
        rightClickOn("#Table2 #tableNameLabel")
        clickOn("#deleteTableMenu")

        then:
        drawBoardController.elements.find { ConnectPolyLine.class.isAssignableFrom(it.getClass())} == null
        drawBoardController.tables().size() == 1
        drawBoardController.tables()[0].is(t1.tableProperty())
    }


}
