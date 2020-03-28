package org.testd.ui.view.dynamic;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.CustomValidator;
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import javafx.scene.Parent;
import org.testd.ui.model.TableMetaProperty;
import org.testd.ui.util.Forms;

import java.util.function.Predicate;

public class TableMetaConfView {

    public static Parent getView(TableMetaProperty metaProperty, Predicate<String> nameValidator) {
        Form form = Form.of(
                Group.of(
                        Field.ofStringType(metaProperty.nameProperty())
                                .id("tableName")
                                .required("Table Name must not be empty")
                                .label("Table Name:")
                                .validate(CustomValidator.forPredicate(nameValidator,
                                        "table name should be unique")),
                        Field.ofBooleanType(metaProperty.virtualProperty())
                                .id("virtual")
                                .label("Virtual:"),
                        Field.ofIntegerType(metaProperty.numberProperty())
                                .id("number")
                                .validate(IntegerRangeValidator.atLeast(0, ">= 0"))
                                .label("Number:")
                )
        ).title("Meta Conf");

        return Forms.renderForm(form);
    }

}
