package org.testd.ui.matchers

import com.dlsc.formsfx.model.structure.SingleSelectionField
import com.dlsc.formsfx.view.controls.SimpleControl
import org.hamcrest.Matcher
import org.testfx.matcher.base.GeneralMatchers

class SimpleControlMatchers {

    static <T> Matcher<SimpleControl> equals(T target) {
        String descriptionText = "equals \"" + target.toString() + "\""
        return GeneralMatchers.typeSafeMatcher(
                SimpleControl.class,
                descriptionText,
                { simpleTextControl ->
                    simpleTextControl.getClass().getSimpleName() + " with : \"" + (
                            simpleTextControl.field instanceof SingleSelectionField ?
                                    simpleTextControl.field.selection
                                    : simpleTextControl.field.value
                    )
                },
                { simpleTextControl ->
                    if (simpleTextControl.field instanceof SingleSelectionField) {
                        def f = simpleTextControl.field as SingleSelectionField
                        if (f.selection == null) {
                            return target == null
                        }
                        return Objects.equals(target,
                                f.selection)
                    }
                    return Objects.equals(
                            target,
                            simpleTextControl.field.value)
                }
        )
    }

}
