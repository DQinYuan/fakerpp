package org.testd.ui.fxweaver;

import org.springframework.context.ConfigurableApplicationContext;
import org.testd.ui.fxweaver.core.FxWeaver;

public class SpringFxWeaver extends FxWeaver {

    public SpringFxWeaver(ConfigurableApplicationContext context) {
        super(context::getBean, context::close);
    }

}
