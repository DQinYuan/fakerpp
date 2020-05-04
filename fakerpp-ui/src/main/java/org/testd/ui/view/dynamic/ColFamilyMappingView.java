package org.testd.ui.view.dynamic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.testd.ui.model.ColProperty;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.ResourceUtil;
import org.testd.ui.view.component.SelectOrUserDefineBox;
import org.testd.ui.vo.ColFamilyVO;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ColFamilyMappingView extends HBox {

    @FXML
    private CheckBox checkbox;

    @FXML
    private SelectOrUserDefineBox mapColBox;

    @FXML
    private ImageView rightArrow;

    private List<String> originColsList = ImmutableList.of();

    private List<StringProperty> mappingColsList = ImmutableList.of();

    @FXML
    private void initialize() {
        rightArrow.setImage(ResourceUtil.loadImage("rightarrow.png"));

        checkbox.selectedProperty().addListener((observable, oldValue, newValue) ->
                toggleMapColVisiable(newValue)
        );
    }

    private void toggleMapColVisiable(boolean isSelected) {
        rightArrow.setVisible(isSelected);
        mapColBox.setVisible(isSelected);
    }

    public void initTargetMutable(String originCol,
                                  String targetCol,
                                  ObjectProperty<TableProperty> targetTable) {
        checkbox.setText(originCol);
        targetTable.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                mapColBox.reinit(ImmutableSet.of(), targetCol);
            } else {
                ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
                setBuilder.addAll(newValue.getExcludes())
                          .addAll(
                                  newValue.getColFamilies().stream()
                                          .map(TableProperty.ColFamilyProperty::getCols)
                                          .flatMap(Collection::stream)
                                          .collect(Collectors.toList())
                          );
                mapColBox.reinit(
                        setBuilder.build(),
                        targetCol);
            }
        });
    }

    public void initTargetImmutable(String originCol,
                                    String targetCol,
                                    TableProperty targetTable,
                                    Set<String> unSelectableCols) {
        checkbox.setText(originCol);
        ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
        setBuilder.addAll(targetTable.getExcludes())
                .addAll(
                        targetTable.getColFamilies().stream()
                                .map(TableProperty.ColFamilyProperty::getCols)
                                .flatMap(Collection::stream)
                                .filter(col -> !unSelectableCols.contains(col))
                                .collect(Collectors.toList())
                );
        mapColBox.reinit(setBuilder.build(), targetCol);
    }

    public boolean selected() {
        return checkbox.isSelected();
    }

    public void select() {
        checkbox.setSelected(true);
    }

    public String getOrigin() {
        return checkbox.getText();
    }

    public String getTarget() {
        return mapColBox.getResult();
    }

    public boolean isUserDefined() {
        return mapColBox.isUserDefined();
    }
}
