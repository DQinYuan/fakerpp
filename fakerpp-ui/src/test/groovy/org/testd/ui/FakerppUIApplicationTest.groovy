package org.testd.ui

import com.dlsc.formsfx.model.structure.Field
import com.dlsc.formsfx.model.structure.Form
import com.dlsc.formsfx.model.structure.Group
import com.dlsc.formsfx.view.renderer.FormRenderer
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.fakerpp.core.ERMLExecutor
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.view.dynamic.ColFamilyView
import org.testd.ui.view.dynamic.JoinView
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

    def "test ColFamilyView"() {
        expect:
        Tools.show(ColFamilyView.class)
    }

    def "test TableView"() {
        expect:
        Tools.show(MyTableView.class)
    }

    def "test JoinView"() {
        expect:
        Tools.show(JoinView.class)
    }

    class Model {
        SimpleStringProperty username = new SimpleStringProperty("")
        SimpleStringProperty password = new SimpleStringProperty("")
    }

    class Person {
        String name
        int age


        @Override
        String toString() {
            return name
        }
    }

    Person of(String name, int age) {
        return new Person(name: name, age: age)
    }

    def "test form"() {
        expect:
        def model = new Model()

/*        model.username.addListener((ChangeListener){ o, oldV, newV ->
            println newV
        })*/

        ObjectProperty<Person> op = new SimpleObjectProperty()

        op.addListener((ChangeListener<? super Person>) { ob, oldV, newV ->
            println(newV)
        })


        Form loginForm = Form.of(
                Group.of(
                        Field.ofStringType(model.username)
                                .label("Username"),
                        Field.ofStringType(model.password)
                                .label("Password")
                                .required("This field can’t be empty"),
                        Field.ofSingleSelectionType(new SimpleListProperty<Person>(
                                FXCollections.<Person> observableArrayList(of("tom", 2),
                                        of("worker", 10))), op)
                                .label("Sex"),
                        Field.ofMultiSelectionType(new SimpleListProperty<String>("aaaaa", "bbbbb"))
                                .label("childs")
                )
        ).title("Login")

        def renderer = new FormRenderer(loginForm)
        Tools.showContent(renderer)

        Platform.exit()
    }

}
