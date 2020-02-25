package org.testd.ui

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.fakerpp.core.ERMLExecutor
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.view.ColFamilyView
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

    def "test fxWeaver"() {
        expect:
        def stage = new Stage()
        def scene = new Scene(fxWeaver.loadControl(ColFamilyView.class))
        stage.setScene(scene)
        stage.showAndWait()
    }

}
