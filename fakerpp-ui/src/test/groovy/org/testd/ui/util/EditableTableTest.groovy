package org.testd.ui.util

import javafx.beans.property.SimpleStringProperty
import javafx.scene.Scene
import javafx.scene.control.TableCell
import javafx.scene.input.KeyCode
import javafx.scene.layout.Pane
import javafx.stage.Stage
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec

class EditableTableTest extends ApplicationSpec {

    Stage testStage

    @Override
    void init() throws Exception {
        FxToolkit.registerStage { new Stage() }
    }

    @Override
    void start(Stage stage) throws Exception {
        testStage = stage
    }

    @Override
    void stop() throws Exception {
        FxToolkit.hideStage()
    }

    def propertify(String str) {
        def ssp = new SimpleStringProperty(str)
/*        ssp.addListener((ChangeListener<? super String>){ ob, old, ne ->
            println(ne)
        })*/
        return ssp
    }

    def "test edit table"() {
        given:
        def content = [[propertify("aaa"), propertify("bbb")],
                       [propertify("ccc"), propertify("ddd")],
                       [propertify("eee"), propertify("fff")]]
        interact({
            def pane = new Pane()
            def table = new EditableTable(content)
            table.setLayoutX(0)
            table.setLayoutY(0)
            pane.getChildren().add(table)

            testStage.setScene(new Scene(pane, 500, 500))

            testStage.show()
        })

        when:
        doubleClickOn(lookup(".table-cell").queryAllAs(TableCell.class)
                .find {it.getText() == "aaa"})
        write("mmmm")
        press(KeyCode.ENTER)

        then:
        content[0][0].get() == "mmmm"
    }


}
