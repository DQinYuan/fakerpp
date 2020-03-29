package org.testd.ui.view

import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.ui.Tools
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.model.TableMetaProperty
import org.testd.ui.view.dynamic.JoinView
import org.testd.ui.view.dynamic.MyTableView
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec

@SpringBootTest
class DrawBoardViewTest extends ApplicationSpec {

    @Autowired
    FxWeaver fxWeaver

    @Autowired
    DrawBoardView drawBoardView

    @Override
    void init() throws Exception {
        FxToolkit.registerStage { new Stage() }
    }

    @Override
    void start(Stage stage) throws Exception {
        stage.setScene(new Scene(drawBoardView, 1000, 600))
        stage.show()
    }

    @Override
    void stop() throws Exception {
        FxToolkit.hideStage()
    }

    def initTablesCfs() {
        def table1 = fxWeaver.loadControl(MyTableView.class)
        table1.initTableMetaProperty(new TableMetaProperty("Table1",
                true,
                10), drawBoardView)
        table1.setTranslateX(10)
        table1.setTranslateY(10)

        def table2 = fxWeaver.loadControl(MyTableView.class)
        table2.initTableMetaProperty(new TableMetaProperty("Table2",
                true,
                30), drawBoardView)
        table2.setTranslateX(350)
        table2.setTranslateY(150)

        interact({
            drawBoardView.append(table1)
            drawBoardView.append(table2)
            table1.setId("Table1")
            table2.setId("Table2")
        })

        Tools.newColFamilies(this, "Table1", """
aaa
bbb
ccc
""")
        Tools.newColFamilies(this, "Table1", """
ll
ee
""")
        Tools.newColFamilies(this, "Table2", """
ddd
eee
fff
""")
        return [table1, table2]
    }

    def "test cascade delete col with join"() {
        given:
        MyTableView t1
        MyTableView t2
        (t1, t2) = initTablesCfs()

        when:
        clickOn("#Table1 #newConnection")
        interact({
            lookup("#targetInput").queryAs(ComboBox.class)
                    .selectionModel.select("Table2")
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

        // check cascade after delete one col
        sendViews[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "bbb", "ll", "ee"] as Set
        recvViews[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "xxb", "ll", "ee"] as Set

        // delete entire col family
        rightClickOn(t1.colFamiliesInput.children[1].colsInput)
        clickOn("#delete_cols_mi")

        // check cascade after delete col family
        sendViews[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "bbb"] as Set
        recvViews[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "xxb"] as Set
    }


}
