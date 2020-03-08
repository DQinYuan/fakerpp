package org.testd.ui.view.dynamic

import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.ui.JavaFXThreadingRule
import org.testd.ui.Tools
import org.testd.ui.fxweaver.core.FxWeaver
import org.testd.ui.model.JoinType
import spock.lang.Specification

@SpringBootTest
class MyTableViewTest extends Specification {

    @Rule
    JavaFXThreadingRule javaFXThreadingRule

    @Autowired
    FxWeaver fxWeaver

    def "test wrapInJoinSendView"() {
        expect:
        List<ColFamilyView> familyViews = Arrays.asList(fxWeaver.loadControl(ColFamilyView.class),
                fxWeaver.loadControl(ColFamilyView.class),
                fxWeaver.loadControl(ColFamilyView.class))
        def myTableView = fxWeaver.loadControl(MyTableView.class)
        myTableView.addTableColFamilies(familyViews)
        myTableView.wrapInJoinSendView("Target", JoinType.RIGHT, familyViews)

        Tools.showContent(myTableView)
    }




}
