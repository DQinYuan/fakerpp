package org.testd.ui;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

/**
 * @author <a href="mailto:rene.gielen@gmail.com">Rene Gielen</a>
 * @noinspection WeakerAccess
 */
public class StageReadyEvent extends ApplicationEvent {

    public StageReadyEvent(Stage stage) {
        // source
        super(stage);
    }

    @Override
    public Stage getSource() {
        return (Stage) super.getSource();
    }
}
