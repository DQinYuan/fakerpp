package org.testd.ui.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.testd.fakerpp.core.parser.ast.Table;
import org.testd.ui.util.ListUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class TableProperty {

    @Getter
    public static class JoinsProperty {
        private final List<JoinProperty> leftJoins;
        private final List<JoinProperty> rightJoins;

        public JoinsProperty() {
            this.leftJoins = new ArrayList<>();
            this.rightJoins = new ArrayList<>();
        }

        public JoinsProperty(List<Table.Join> leftJoins, List<Table.Join> rightJoins) {
            this.leftJoins = ListUtil.map(leftJoins, JoinProperty::map);
            this.rightJoins = ListUtil.map(rightJoins, JoinProperty::map);
        }
    }

    @Getter
    public static class JoinProperty {
        private final Map<String, String> map;
        private final StringProperty depend;
        private final BooleanProperty random;

        public JoinProperty(Map<String, String> map, String depend, boolean random) {
            this.map = new HashMap<>(map);
            this.depend = new SimpleStringProperty(depend);
            this.random = new SimpleBooleanProperty(random);
        }

        private static JoinProperty map(Table.Join join) {
            return new JoinProperty(join.getMap(), join.getDepend(), join.isRandom());
        }
    }

    @Getter
    public static class ColFamilyProperty {
        private final ObservableList<String> cols;
        private final ObservableList<GeneratorInfoProperty> generatorInfos;

        public ColFamilyProperty(List<String> cols, List<GeneratorInfoProperty> generatorInfos) {
            this.cols = FXCollections.observableArrayList(cols);
            this.generatorInfos = FXCollections.observableArrayList(generatorInfos);
        }

        private static ColFamilyProperty map(Table.ColFamily colFamily) {
            return new ColFamilyProperty(colFamily.getCols(),
                    ListUtil.map(colFamily.getGeneratorInfos(), GeneratorInfoProperty::map));
        }
    }

    @Getter
    public static class GeneratorInfoProperty {
        private final StringProperty field;
        private final StringProperty lang;
        private final StringProperty generator;
        private final Map<String, String> attributes;
        private final List<List<String>> options;
        private final IntegerProperty weight;

        private static String DEFAULT_FIELD = "built-in";
        private static String DEFAULT_GENERATOR = "str";

        public static GeneratorInfoProperty defaultProperty() {
            return new TableProperty.GeneratorInfoProperty(DEFAULT_FIELD,
                    "",   // use global default lang
                    DEFAULT_GENERATOR,
                    new HashMap<>(),
                    new ArrayList<>(),
                    1);
        }

        public GeneratorInfoProperty(String field, String lang, String generator,
                                     Map<String, String> attributes,
                                     List<List<String>> options, int weight) {
            this.field = new SimpleStringProperty(field);
            this.lang = new SimpleStringProperty(lang);
            this.generator = new SimpleStringProperty(generator);
            this.attributes = new HashMap<>(attributes);
            this.options = new ArrayList<>(options);
            this.weight = new SimpleIntegerProperty(weight);
        }

        private static GeneratorInfoProperty map(Table.GeneratorInfo generatorInfo) {
            return new GeneratorInfoProperty(generatorInfo.getField(),
                    generatorInfo.getLang(), generatorInfo.getGenerator(),
                    generatorInfo.getAttributes(), generatorInfo.getOptions(), generatorInfo.getWeight());
        }

    }

    private final StringProperty name;
    private final ObjectProperty<DataSourceInfoProperty> ds;
    private final IntegerProperty num;

    private final JoinsProperty joins;
    private final List<ColFamilyProperty> colFamilies;
    private final List<String> excludes;

    public TableProperty(String name) {
        this.name = new SimpleStringProperty(name);
        this.ds = new SimpleObjectProperty<>();
        this.num = new SimpleIntegerProperty();
        this.joins = new JoinsProperty();
        this.colFamilies = new ArrayList<>();
        this.excludes = new ArrayList<>();
    }

    private TableProperty(String name, DataSourceInfoProperty ds,
                         int num, Table.Joins joins,
                         List<Table.ColFamily> colFamilies,
                         List<String> excludes) {
        this.name = new SimpleStringProperty(name);
        this.ds = new SimpleObjectProperty<>(ds);
        this.num = new SimpleIntegerProperty(num);

        this.joins = new JoinsProperty(joins.getLeftJoins(),
                joins.getRightJoins());
        this.colFamilies = ListUtil.map(colFamilies, ColFamilyProperty::map);
        this.excludes = new ArrayList<>(excludes);
    }

    public static TableProperty map(Table table, DataSourceInfoProperty ds) {
        return new TableProperty(
                table.getName(),
                ds,
                table.getNum(),
                table.getJoins(),
                table.getColFamilies(),
                table.getExcludes()
        );
    }

}
