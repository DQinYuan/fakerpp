package org.testd.ui.view.dynamic;

import com.google.common.collect.ImmutableList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxmlView;

import java.util.List;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ColFamilyMappingView extends HBox {

    @FXML
    private CheckBox checkbox;

    @FXML
    private TextField mapCol;

    @FXML
    private ImageView rightArrow;

    private List<String> originColsList = ImmutableList.of();

    private List<StringProperty> mappingColsList = ImmutableList.of();

    @FXML
    private void initialize() {
        rightArrow.setImage(new Image(ColFamilyMappingView.class
                .getResource("/img/rightarrow.png").toString()));

        checkbox.selectedProperty().addListener((observable, oldValue, newValue) ->
            toggleMapColVisiable(newValue)
        );
    }

    private void toggleMapColVisiable(boolean isSelected) {
        rightArrow.setVisible(isSelected);
        mapCol.setVisible(isSelected);
    }

    public void initFromOriginAndMappingCol(String originCol, String targetCol) {
        checkbox.setText(originCol);
        mapCol.setText(targetCol);
    }

    public boolean selected() {
        return checkbox.isSelected();
    }

    public BooleanProperty selectedProperty() {
        return checkbox.selectedProperty();
    }

    public void select() {
        checkbox.setSelected(true);
    }

    public String getOrigin() {
        return checkbox.getText();
    }

    public String getTarget() {
        return mapCol.getText();
    }
}
