package org.testd.ui.view.form

import javafx.beans.property.StringProperty
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.fakerpp.core.engine.generator.Generator
import org.testd.fakerpp.core.engine.generator.GeneratorSupplier
import org.testd.fakerpp.core.parser.ast.Table
import org.testd.ui.model.TableProperty
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec

@SpringBootTest
class EditColFamilyParamViewTest extends ApplicationSpec {

    @Autowired
    EditColFamilyParamView editColFamilyParamView

    Stage testStage

    @Override
    void init() throws Exception {
        FxToolkit.registerStage { new Stage() }
    }

    @Override
    void start(Stage stage) throws Exception {
        testStage = stage
    }

    @Override
    void stop() throws Exception {
        FxToolkit.cleanupStages()
    }

    GeneratorSupplier mockGeneratorSupplier(Map<String, GeneratorSupplier.ParamInfo> paramInfos,
                                            List<List<String>> options) {
        return new GeneratorSupplier() {
            @Override
            Generator generator(String lang) {
                return null
            }

            @Override
            Map<String, GeneratorSupplier.ParamInfo> paramInfos() {
                return paramInfos
            }

            @Override
            Optional<GeneratorSupplier.ParamSetter<List<List<String>>>> optionSetter() {
                if (options == null) {
                    return Optional.empty()
                } else {
                    return Optional.of(
                            new GeneratorSupplier.ParamSetter<List<List<String>>>() {
                                @Override
                                void setValue(Generator generator, List<List<String>> value) {
                                    throw new RuntimeException()
                                }
                            }
                    )
                }
            }
        }
    }

    GeneratorSupplier.ParamInfo mockParamInfo(String name, Class<?> relType,
                                              Object defaultValue, boolean multiLine) {
        return new GeneratorSupplier.ParamInfo(name, relType, defaultValue, multiLine) {
            @Override
            void setValue(Generator generator, String value) {
                throw new RuntimeException()
            }
        }
    }

    Map<String, String> valueSp2S(Map<String, StringProperty> spMap) {
        return spMap.collectEntries {
            [(it.getKey()):it.getValue().get()]
        } as Map<String, String>
    }

    def "test edit col family param"() {
        given:
        TableProperty.GeneratorInfoProperty gInfo = TableProperty
                .GeneratorInfoProperty.defaultProperty()

        Closure<Region> formGetter = {
            editColFamilyParamView.getView(
                    gInfo,
                    mockGeneratorSupplier(
                            ["str" : mockParamInfo("str", String.class, "lala", false),
                             "enum": mockParamInfo("enum", String.class, ["a", "b"] as String[],
                                     false)],
                            null
                    ),
                    testStage
            )
        }
        interact({
            testStage.setScene(new Scene(formGetter(), 600, 400))
            testStage.show()
        })

        when:
        // edti
        interact({
            lookup("#str .text-field").queryAs(TextField.class).setText("kaka")
            lookup("#enum .combo-box").queryAs(ComboBox.class).selectionModel.select("b")
        })

        then:
        valueSp2S(gInfo.attributes) == ["str":"lala", "enum":"a"]

        and:
        clickOn("#formOkButton")

        then:
        valueSp2S(gInfo.attributes) == [
                "str":"kaka",
                "enum": "b"
        ]
    }
}
