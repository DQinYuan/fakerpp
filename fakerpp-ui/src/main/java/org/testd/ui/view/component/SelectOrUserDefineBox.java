package org.testd.ui.view.component;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import java.util.Set;

public class SelectOrUserDefineBox extends HBox {

    private static final String userDefineInfo = "(create new col)";

    private final ComboBox<String> select;
    private final TextField userDefine;

    public SelectOrUserDefineBox() {
        setSpacing(10);
        select = new ComboBox<>();

        userDefine = new TextField();
        userDefine.setVisible(false);
        userDefine.setMaxWidth(100);
    }

    private Glyph userDefineIco() {
        return Glyph.create("FontAwesome|" +
                FontAwesome.Glyph.USER_PLUS.name()).sizeFactor(1)
                .color(Color.BLACK).useGradientEffect();
    }

    public void reinit(Set<String> readOnlyOptions, String defaul) {
        getChildren().clear();
        select.getItems().clear();

        select.getItems().addAll(readOnlyOptions);
        select.getItems().add(userDefineInfo);

        Callback<ListView<String>, ListCell<String>> cellFactory = listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (userDefineInfo.equals(item)) {
                    setText(null);
                    setGraphic(userDefineIco());
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
        };

        select.setButtonCell(cellFactory.call(null));
        select.setCellFactory(cellFactory);

        userDefine.visibleProperty()
                .bind(select.valueProperty().isEqualTo(userDefineInfo));

        getChildren().addAll(select, userDefine);

        if (readOnlyOptions.contains(defaul)) {
            select.getSelectionModel().select(defaul);
        } else {
            select.getSelectionModel().select(userDefineInfo);
            userDefine.setText(defaul);
        }
    }

    public String getResult() {
        String selected = select.getValue();
        if (userDefineInfo.equals(selected)) {
            return userDefine.getText();
        }

        return selected;
    }

    public boolean isUserDefined() {
        return userDefineInfo.equals(select.getValue());
    }

}
