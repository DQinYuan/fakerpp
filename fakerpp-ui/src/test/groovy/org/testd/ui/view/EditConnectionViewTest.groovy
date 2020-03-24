package org.testd.ui.view

import javafx.application.Platform
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.ui.JavaFXThreadingRule
import org.testd.ui.Tools
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.view.dynamic.EditConnectionView
import spock.lang.Specification

@SpringBootTest
class EditConnectionViewTest extends Specification {

    @Autowired
    FxWeaver fxWeaver

    @Rule
    JavaFXThreadingRule javaFXThreadingRule

    def "test new connection view"() {
        expect:
        Tools.showContent(fxWeaver.loadView(EditConnectionView.class))
        Platform.exit()
    }

}
