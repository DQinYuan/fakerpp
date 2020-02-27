package org.testd.ui

import com.dlsc.formsfx.model.structure.Field
import com.dlsc.formsfx.model.structure.Form
import com.dlsc.formsfx.model.structure.Group
import com.dlsc.formsfx.view.renderer.FormRenderer
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Scene
import javafx.stage.Stage
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.fakerpp.core.ERMLExecutor
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.view.dynamic.ColFamilyView
import org.testd.ui.view.dynamic.JoinReceiveView
import org.testd.ui.view.dynamic.MyTableView
import spock.lang.Specification

@SpringBootTest
class FakerppUIApplicationTest extends Specification {

    @Autowired
    ERMLExecutor executor

    @Autowired
    DefaultsConfig defaultsConfig

    @Autowired
    FxWeaver fxWeaver

    @Rule
    JavaFXThreadingRule javaFXThreadingRule


    def "test di"() {
        expect:
        println(defaultsConfig.getLocalesInfo().getDefaultLocale())
        println(defaultsConfig.getLocalesInfo().getSupportedLocales())
        println(defaultsConfig.getBatchSize())
    }

    def showContent(content) {
        def stage = new Stage()
        def scene = new Scene(content)
        stage.setScene(scene)
        stage.showAndWait()
        return true
    }

    def show(Class<?> viewClass) {
        return showContent(fxWeaver.loadControl(viewClass))
    }

    def "test ColFamilyView"() {
        expect:
        show(ColFamilyView.class)
    }

    def "test TableView"() {
        expect:
        show(MyTableView.class)
    }

    def "test JoinView"() {
        expect:
        show(JoinReceiveView.class)
    }

    class Model {
        SimpleStringProperty username = new SimpleStringProperty("")
        SimpleStringProperty password = new SimpleStringProperty("")
    }

    def "test form"() {
        expect:
        def model = new Model()

/*        model.username.addListener((ChangeListener){ o, oldV, newV ->
            println newV
        })*/

        Form loginForm = Form.of(
                Group.of(
                        Field.ofStringType(model.username)
                                .label("Username"),
                        Field.ofStringType(model.password)
                                .label("Password")
                                .required("This field can’t be empty")
                )
        ).title("Login")

        def renderer = new FormRenderer(loginForm)
        showContent(renderer)

        Platform.exit()
    }

}
