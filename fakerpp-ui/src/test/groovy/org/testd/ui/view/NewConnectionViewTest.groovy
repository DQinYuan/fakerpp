package org.testd.ui.view

import javafx.application.Platform
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.ui.JavaFXThreadingRule
import org.testd.ui.Tools
import org.testd.ui.fxweaver.core.FxWeaver
import spock.lang.Specification

@SpringBootTest
class NewConnectionViewTest extends Specification {

    @Autowired
    FxWeaver fxWeaver

    @Rule
    JavaFXThreadingRule javaFXThreadingRule

    def "test new connection view"() {
        expect:
        Tools.showContent(fxWeaver.loadView(NewConnectionView.class))
        Platform.exit()
    }

}
