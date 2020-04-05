package org.testd.ui.view.dynamic

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Spinner
import javafx.stage.Stage
import org.testd.fakerpp.core.parser.ast.DataSourceInfo
import org.testd.ui.controller.MetaController
import org.testd.ui.matchers.SimpleControlMatchers
import org.testd.ui.model.DataSourceInfoProperty
import org.testd.ui.view.form.TableMetaConfView
import org.testd.ui.vo.TableMetaVO
import org.testfx.api.FxAssert
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec
import spock.lang.Shared

class TableMetaConfViewTest extends ApplicationSpec {

    Stage testStage


    TableMetaVO tableMetaVO

    @Shared
    TableMetaConfView tableMetaConfView

    @Shared
    ObservableList<DataSourceInfoProperty> dataSourceInfos =
            FXCollections.observableArrayList(
                    new DataSourceInfoProperty(new DataSourceInfo("aaaa", "", "",
                            10, "", "", "")),
                    new DataSourceInfoProperty(new DataSourceInfo("bbb", "", "",
                            10, "", "", "")),
                    new DataSourceInfoProperty(new DataSourceInfo("cc", "", "",
                            10, "", "", "")),
                    new DataSourceInfoProperty(new DataSourceInfo("ddddd", "", "",
                            10, "", "", "")),
            )

    def setupSpec() {
        def mockMetaCon = Mock(MetaController)
        mockMetaCon.getDsInfos() >> dataSourceInfos
        tableMetaConfView = new TableMetaConfView(mockMetaCon)
    }

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

    def nonVirtualMeta() {
        interact({
            StringProperty name = new SimpleStringProperty("TableMetaConfViewTest")
            ObjectProperty<DataSourceInfoProperty> ds =
                    new SimpleObjectProperty<>(dataSourceInfos[0])
            IntegerProperty num = new SimpleIntegerProperty(10)
            tableMetaVO = new TableMetaVO(name, ds, num)
            testStage.setScene(new Scene(tableMetaConfView.getView(
                    tableMetaVO,
                    { true }
            ), 800, 600))

            testStage.show()
        })
    }

    def virtualMeta() {
        interact({
            StringProperty name = new SimpleStringProperty("TableMetaConfViewTest")
            ObjectProperty<DataSourceInfoProperty> ds =
                    new SimpleObjectProperty<>(null)
            IntegerProperty num = new SimpleIntegerProperty(10)
            tableMetaVO = new TableMetaVO(name, ds, num)
            testStage.setScene(new Scene(tableMetaConfView.getView(
                    tableMetaVO,
                    { true }
            ), 800, 600))

            testStage.show()
        })
    }

    def "meta conf display"() {
        when:
        nonVirtualMeta()

        then:
        FxAssert.verifyThat("#tableName",
                SimpleControlMatchers.equals("TableMetaConfViewTest"))
        FxAssert.verifyThat("#virtual",
                SimpleControlMatchers.equals(false))
        FxAssert.verifyThat("#dataSource",
                SimpleControlMatchers.equals(dataSourceInfos[0]))
        FxAssert.verifyThat("#number",
                SimpleControlMatchers.equals(10))
    }

    def "form lazy persist"() {
        given:
        nonVirtualMeta()

        when:
        interact({
            lookup("#dataSource .combo-box")
                    .queryAs(ComboBox.class).selectionModel.select(dataSourceInfos[1])
            lookup("#number .spinner")
                    .queryAs(Spinner.class).increment(11)
        })

        then:
        tableMetaVO.nameProperty().get() == "TableMetaConfViewTest"
        tableMetaVO.dataSourceProperty().get() == dataSourceInfos[0]
        tableMetaVO.numberProperty().get() == 10
    }

    def "edit meta conf"() {
        given:
        nonVirtualMeta()

        when:
        interact({
            lookup("#dataSource .combo-box")
                    .queryAs(ComboBox.class).selectionModel.select(dataSourceInfos[1])
            lookup("#number .spinner")
                    .queryAs(Spinner.class).increment(11)
        })
        clickOn("#formOkButton")

        then:
        tableMetaVO.nameProperty().get() == "TableMetaConfViewTest"
        tableMetaVO.dataSourceProperty().get() == dataSourceInfos[1]
        tableMetaVO.numberProperty().get() == 21
    }

    def "virtual meta conf"() {
        when:
        virtualMeta()

        then:
        FxAssert.verifyThat("#virtual",
                SimpleControlMatchers.equals(true))
        lookup("#dataSource .combo-box").queryAs(ComboBox.class).visible == false
    }

    def "non virtual table must have a data source"() {
        given:
        nonVirtualMeta()

        when:
        clickOn("#virtual .check-box")
        clickOn("#virtual .check-box")

        then:
        lookup("#formOkButton").queryAs(Button.class).disable == true
    }

}
