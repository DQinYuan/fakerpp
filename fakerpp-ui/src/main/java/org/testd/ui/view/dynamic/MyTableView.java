package org.testd.ui.view.dynamic;

import com.dlsc.formsfx.model.structure.*;
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.TableMetaProperty;
import org.testd.ui.util.Forms;
import org.testd.ui.view.MainWindowView;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MyTableView extends BorderPane {

    private final FxWeaver fxWeaver;
    private final MainWindowView mainWindowView;
    private final PrimaryStageHolder primaryStageHolder;

    @FXML
    private VBox colFamiliesInput;

    @FXML
    private ToolBar toolBar;

    @FXML
    private MenuItem deleteTableMenu;

    @FXML
    private void initialize() {
        colFamiliesInput.getChildren().add(fxWeaver.loadControl(ColFamilyView.class));
        colFamiliesInput.getChildren().add(fxWeaver.loadControl(ColFamilyView.class));
        colFamiliesInput.getChildren().add(fxWeaver.loadControl(JoinReceiveView.class));
        colFamiliesInput.getChildren().add(fxWeaver.loadControl(ColFamilyView.class));
        colFamiliesInput.getChildren().add(fxWeaver.loadControl(JoinSendView.class));

        dragable();

        deleteTableMenu.setOnAction(event -> mainWindowView.deleteTableFromDrawBoard(this));
    }

    private static class Delta {
        double x, y;
    }

    private void dragable() {
        final Delta dragDelta = new Delta();
        this.setOnMousePressed(mouseEvent -> {
            dragDelta.x = this.getTranslateX() - mouseEvent.getSceneX();
            dragDelta.y = this.getTranslateY() - mouseEvent.getSceneY();

            this.setCursor(Cursor.NONE);
            this.toFront();
        });
        this.setOnMouseReleased(mouseEvent -> this.setCursor(Cursor.HAND));
        this.setOnMouseDragged(mouseEvent -> {
            double translateX = mouseEvent.getSceneX() + dragDelta.x;
            double translateY = mouseEvent.getSceneY() + dragDelta.y;

            this.setTranslateX(translateX);

            this.setTranslateY(translateY);
        });
    }

    @FXML
    private void handleMetaConf() {
        TableMetaProperty metaProperty = new TableMetaProperty();

        Form form = Form.of(
                Group.of(
                        Field.ofStringType(metaProperty.nameProperty())
                                .required("Table Name must not be empty")
                                .label("Table Name:"),
                        Field.ofBooleanType(metaProperty.virtualProperty()).label("Virtual:"),
                        Field.ofIntegerType(metaProperty.numberProperty())
                                .validate(IntegerRangeValidator.atLeast(0, ">= 0"))
                                .label("Number:")
                )
        ).title("Meta Conf");

        primaryStageHolder.newSceneInChild(Forms.renderForm(form));
    }

}
