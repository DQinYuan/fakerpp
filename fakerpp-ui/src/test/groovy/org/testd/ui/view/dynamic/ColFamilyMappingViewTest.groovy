package org.testd.ui.view.dynamic

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.ui.JavaFXThreadingRule
import org.testd.ui.Tools
import org.testd.ui.fxweaver.core.FxWeaver
import spock.lang.Specification

@SpringBootTest
class ColFamilyMappingViewTest extends Specification {

    @Rule
    JavaFXThreadingRule javaFXThreadingRule

    @Autowired
    FxWeaver fxWeaver

    def "test view"() {
        expect:
        def mappingView = fxWeaver.loadControl(ColFamilyMappingView.class)
        mappingView.initFromOriginAndMappingColFamily(["col1", "col2", "col3"],
                [new SimpleStringProperty("col1"), new SimpleStringProperty("col2"),
                 new SimpleStringProperty("col3")])
        Tools.showContent(mappingView)
        Platform.exit()
    }

}
