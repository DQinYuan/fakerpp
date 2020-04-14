package org.testd.ui.util;

import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.Button;

public class ButtonAbler {

    private final Button relatedButton;

    private BooleanBinding andBinding;

    public ButtonAbler(Button relatedButton) {
        this.relatedButton = relatedButton;
        this.andBinding = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return true;
            }
        };
    }

    public void addPredicate(BooleanBinding booleanBinding) {
        this.andBinding = andBinding.and(booleanBinding);
    }

    public void build() {
        andBinding.addListener((observable, oldValue, newValue) -> {
            relatedButton.setDisable(!newValue);
        });
        relatedButton.setDisable(!andBinding.get());
    }
}
