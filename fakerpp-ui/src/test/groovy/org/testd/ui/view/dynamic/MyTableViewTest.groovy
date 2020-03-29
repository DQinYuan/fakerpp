package org.testd.ui.view.dynamic

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.ui.Tools
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.matchers.SimpleControlMatchers
import org.testd.ui.model.TableMetaProperty
import org.testfx.api.FxAssert
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec
import org.testfx.matcher.control.LabeledMatchers

@SpringBootTest
class MyTableViewTest extends ApplicationSpec {

    @Autowired
    FxWeaver fxWeaver

    MyTableView myTableView

    @Override
    void init() throws Exception {
        FxToolkit.registerStage { new Stage() }
    }

    @Override
    void start(Stage stage) throws Exception {
        myTableView = fxWeaver.loadControl(MyTableView.class)
        myTableView.initTableMetaProperty(new TableMetaProperty("MyTableViewTest",
                true,
                10), new Pane())
        stage.setScene(new Scene(myTableView, 600, 400))
        stage.show()
    }

    @Override
    void stop() throws Exception {
        FxToolkit.hideStage()
    }

    def "meta conf display"() {
        expect:
        FxAssert.verifyThat("#tableNameLabel",
                LabeledMatchers.hasText("MyTableViewTest"))
        clickOn("#editMetaConf")
        FxAssert.verifyThat("#tableName",
                SimpleControlMatchers.equals("MyTableViewTest"))
        FxAssert.verifyThat("#virtual",
                SimpleControlMatchers.equals(true))
        FxAssert.verifyThat("#number",
                SimpleControlMatchers.equals(10))
    }

    def "meta conf edit"() {
        when:
        clickOn("#editMetaConf")
        // select child element
        clickOn("#virtual .check-box")
        clickOn("#formOkButton")
        clickOn("#editMetaConf")

        then:
        FxAssert.verifyThat("#tableName",
                SimpleControlMatchers.equals("MyTableViewTest"))
        FxAssert.verifyThat("#virtual",
                SimpleControlMatchers.equals(false))
        FxAssert.verifyThat("#number",
                SimpleControlMatchers.equals(10))
    }

    def "add new col family"() {
        when:
        Tools.newColFamilies(this, """
aaa
bbb
ccc
""")

        then:
        myTableView.getColFamilies().size() == 1
        myTableView.getColFamilies()[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "bbb", "ccc"] as Set
    }

    def "open new col family dialog but not add"() {
        when:
        clickOn("#newColFamily")
        Platform.runLater({ ((Stage) targetWindow()).close() })

        then:
        myTableView.getColFamilies().size() == 0
    }

    def "col families move"() {
        given:
        Tools.newColFamilies(this, """
aaa
bbb
""")
        Tools.newColFamilies(this, """
ddd
ee
""")

        when:
        // popup menu
        rightClickOn(myTableView.colFamiliesInput.children[1].colsInput)
        // click on popup menu
        clickOn("#edit_cols_mi")
        lookup(".check-box")
                .queryAll().each { clickOn(it) }
        clickOn("#newCols")
        write("\nlala")
        clickOn("#okButton")

        then:
        myTableView.colFamilies.size() == 1
        myTableView.colFamilies[0].colsProperty()*.getColName().toSet() ==
                ["aaa", "bbb", "ddd", "ee", "lala"] as Set
    }

    def "duplicate col with other col families will pop up error dialog"() {
        given:
        Tools.newColFamilies(this, """
aaa
""")
        when:
        clickOn("#newColFamily")
        clickOn("#newCols")
        write("aaa")
        clickOn("#okButton")

        then:
        myTableView.colFamilies.size() == 1
        listTargetWindows().size() == 3
        interact({((Stage)listTargetWindows()[2]).close()})
    }

    def "duplicate col with self will reserve only one"() {
        when:
        Tools.newColFamilies(this, """
aaa
aaa
""")
        then:
        myTableView.getColFamilies().size() == 1
        myTableView.getColFamilies()[0].colsProperty()*.getColName() == ["aaa"]
    }
}
