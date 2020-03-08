package org.testd.ui.view.dynamic;

import com.dlsc.formsfx.model.structure.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.util.Forms;
import org.testd.ui.util.Stages;

import java.util.List;
import java.util.stream.Collectors;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ColFamilyMappingView extends HBox {

    @FXML
    private CheckBox checkbox;

    @FXML
    private Label mappingCols;

    @FXML
    private ImageView rightArrow;

    private List<String> originColsList = ImmutableList.of();

    private List<StringProperty> mappingColsList = ImmutableList.of();

    private FollowRightMouseMenu editMappingMenu = new FollowRightMouseMenu(true, this,
            FollowRightMouseMenu.menuEntry("edit mapping",
                    ignore -> this::editMappingAction));

    private void editMappingAction(ActionEvent event) {
        Element[] fields = Streams.zip(originColsList.stream(), mappingColsList.stream(),
                this::mapField).toArray(Element[]::new);

        Stages.newSceneInChild(
                Forms.renderForm(Form.of(Group.of(fields))), getScene().getWindow()
        );
    }

    private StringField mapField(String originCol, StringProperty fieldProp) {
        return Field.ofStringType(fieldProp)
                .label(originCol + " → ")
                .required("map col can not be empty");
    }

    @FXML
    private void initialize() {
        rightArrow.setImage(new Image(ColFamilyMappingView.class
                .getResource("/img/rightarrow.png").toString()));

        checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                rightArrow.setVisible(true);
                mappingCols.setVisible(true);
                addEventHandler(MouseEvent.MOUSE_CLICKED, editMappingMenu);
            } else {
                rightArrow.setVisible(false);
                mappingCols.setVisible(false);
                removeEventHandler(MouseEvent.MOUSE_CLICKED, editMappingMenu);
            }
        });
    }

    public void initFromOriginAndMappingColFamily(List<String> originCols, List<StringProperty> cols) {
        if (originCols.size() != cols.size()) {
            throw new IllegalArgumentException("origin cols number and mapping cols number should be identical");
        }
        this.originColsList = originCols;
        this.mappingColsList = cols;

        checkbox.setText(originCols.stream().collect(Collectors.joining(",", "[", "]")));
        mappingCols.setText(colFamiliesString(cols));

        cols.forEach(this::registerCol);
    }

    private void registerCol(StringProperty sp) {
        sp.addListener((observable, oldValue, newValue) ->
                mappingCols.setText(colFamiliesString(mappingColsList))
        );
    }

    private String colFamiliesString(List<StringProperty> cols) {
        return cols.stream().map(StringProperty::get)
                .collect(Collectors.joining(",", "[", "]"));
    }

    public void initFromOriginTableView(MyTableView tableView) {

    }

}
