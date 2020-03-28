package org.testd.ui.matchers

import com.dlsc.formsfx.view.controls.SimpleControl
import org.hamcrest.Matcher
import org.testfx.matcher.base.GeneralMatchers

class SimpleControlMatchers {

    static <T> Matcher<SimpleControl> equals(T target) {
        String descriptionText = "equals \"" + target.toString() + "\""
        return GeneralMatchers.typeSafeMatcher(
                SimpleControl.class,
                descriptionText,
                {simpleTextControl ->
                    simpleTextControl.getClass().getSimpleName() + " with : \"" +
                            simpleTextControl.field.value
                },
                {simpleTextControl -> Objects.equals(
                        target,
                        simpleTextControl.field.value
                )}
        )
    }

}
