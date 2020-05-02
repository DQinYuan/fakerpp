package org.testd.ui.model;

import com.google.common.collect.ImmutableMap;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.javatuples.Pair;
import org.joox.Match;
import org.springframework.util.CollectionUtils;
import org.testd.fakerpp.core.parser.ast.Table;
import org.testd.fakerpp.core.util.MyMapUtil;
import org.testd.ui.util.ListUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Collectors;

import static org.joox.JOOX.$;

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

        public Table.Joins unmap() {
            return new Table.Joins(
                    leftJoins.stream().map(JoinProperty::unmap).collect(Collectors.toList()),
                    rightJoins.stream().map(JoinProperty::unmap).collect(Collectors.toList())
            );
        }

        private Element joinElement(Document document, JoinProperty jp, JoinType joinType) {
            Element joinEle = joinType == JoinType.LEFT? document.createElement("leftjoin"):
                    document.createElement("rightjoin");

            Match join = $(joinEle)
                    .attr("depend", jp.getDepend().get());
            jp.getMap().forEach((from, to) ->
                    join.append(
                            $(document.createElement("map"))
                                    .attr("from", from)
                                    .content(to)
                    )
            );
            return joinEle;
        }

        public Element serial(Document document) {
            Element joins = document.createElement("joins");
            leftJoins.forEach(
                    jp -> $(joins).append(joinElement(document, jp, JoinType.LEFT))
            );

            rightJoins.forEach(
                    jp -> {
                        Element join = joinElement(document, jp, JoinType.RIGHT);
                        $(join).attr("random", String.valueOf(jp.random.get()));
                        $(joins).append(join);
                    }
            );

            return joins;
        }

    }

    @Getter
    public static class JoinProperty {
        private final Map<String, String> map;
        private final StringProperty depend;
        private final BooleanProperty random;

        public JoinProperty(Map<String, String> map, String depend, boolean random) {
            this.map = new LinkedHashMap<>(map);
            this.depend = new SimpleStringProperty(depend);
            this.random = new SimpleBooleanProperty(random);
        }

        public static JoinProperty defaultProperty(String dependTableName) {
            return new JoinProperty(ImmutableMap.of(), dependTableName, false);
        }

        private static JoinProperty map(Table.Join join) {
            return new JoinProperty(join.getMap(), join.getDepend(), join.isRandom());
        }

        public Table.Join unmap() {
            return new Table.Join(
                    map,
                    depend.get(),
                    random.get()
            );
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

        public ColFamilyProperty(ObservableList<String> cols, ObservableList<GeneratorInfoProperty> generatorInfos) {
            this.cols = cols;
            this.generatorInfos = generatorInfos;
        }

        private static ColFamilyProperty map(Table.ColFamily colFamily) {
            return new ColFamilyProperty(colFamily.getCols(),
                    ListUtil.map(colFamily.getGeneratorInfos(), GeneratorInfoProperty::map));
        }

        public Table.ColFamily unmap() {
            return new Table.ColFamily(
                    cols,
                    getGeneratorInfos().stream().map(GeneratorInfoProperty::unmap).collect(Collectors.toList())
            );
        }

        public Element serial(Document document) {
            assert generatorInfos.size() > 0;

            Match colsEle = $(document.createElement("cols")).append(getCols().stream().map(
                    col -> $(document.createElement("col")).content(col)
            ).toArray(Match[]::new));

            if (generatorInfos.size() == 1) {
                GeneratorInfoProperty generatorInfoProperty = generatorInfos.get(0);
                Pair<Element, Element> genInfoSerial = generatorInfoProperty.serial(document);
                Element generatorEle = genInfoSerial.getValue1();
                $(generatorEle).append(colsEle);
                return genInfoSerial.getValue0();
            }

            Element root = document.createElement("composes");
            $(root).append(colsEle)
                   .append(
                           getGeneratorInfos().stream().map(
                                   gp -> $(document.createElement("compose"))
                                   .attr("weight", String.valueOf(gp.getWeight().get()))
                                   .append(gp.serial(document).getValue0())
                           ).toArray(Match[]::new)
                   );

            return root;
        }

    }

    @Getter
    public static class GeneratorInfoProperty {
        private final StringProperty field;
        private final StringProperty lang;
        private final StringProperty generator;
        private final Map<String, StringProperty> attributes;
        private final List<List<StringProperty>> options;
        private final IntegerProperty weight;

        private static String DEFAULT_FIELD = "built-in";
        private static String DEFAULT_GENERATOR = "str";

        public static GeneratorInfoProperty defaultProperty() {
            return new TableProperty.GeneratorInfoProperty(DEFAULT_FIELD,
                    Table.GeneratorInfo.FOLLOW_DEFAULT_LANG,   // use global default lang
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
            this.attributes = MyMapUtil.mutableValueMap(attributes,
                    SimpleStringProperty::new);
            this.options = options.stream()
                    .map(l -> l.stream().map(s -> (StringProperty) (new SimpleStringProperty(s))).collect(Collectors.toList()))
                    .collect(Collectors.toList());
            this.weight = new SimpleIntegerProperty(weight);
        }

        private static GeneratorInfoProperty map(Table.GeneratorInfo generatorInfo) {
            return new GeneratorInfoProperty(generatorInfo.getField(),
                    generatorInfo.getLang(), generatorInfo.getGenerator(),
                    generatorInfo.getAttributes(), generatorInfo.getOptions(), generatorInfo.getWeight());
        }

        public Table.GeneratorInfo unmap() {
            return new Table.GeneratorInfo(
                    field.get(),
                    lang.get(),
                    generator.get(),
                    MyMapUtil.valueMap(attributes, StringProperty::get),
                    options.stream().map(l -> l.stream().map(StringProperty::get).collect(Collectors.toList()))
                            .collect(Collectors.toList()),
                    weight.get()
            );
        }

        /**
         * @param document
         * @return {@code Pair<generator root element, generator element>}
         */
        public Pair<Element, Element> serial(Document document) {
            Element root = document.createElement(getField().get());

            Element generatorEle = document.createElement(getGenerator().get());
            attributes.forEach((attrName, attrVal) ->
                    $(generatorEle).attr(attrName, attrVal.get())
            );

            if (!CollectionUtils.isEmpty(options)) {
                Match optionsEle =
                        $(document.createElement("options"))
                                .append(
                                        options.stream().map(cells ->
                                                $(document.createElement("option")).append(
                                                        cells.stream().map(cell ->
                                                                $(document.createElement("cell"))
                                                                        .content(cell.get())
                                                        ).toArray(Match[]::new)
                                                )
                                        ).toArray(Match[]::new)
                                );
                $(generatorEle).append(optionsEle);
            }

            if (!"built-in".equals(getField().get())) {
                $(root).attr("lang", getLang().get());
            }
            $(root).append(generatorEle);

            return new Pair<>(root, generatorEle);
        }

        public void clearParam() {
            attributes.clear();
            options.clear();
        }

    }

    private final StringProperty name;
    private final ObjectProperty<DataSourceInfoProperty> ds;
    private final IntegerProperty num;

    private final JoinsProperty joins;
    private final Set<ColFamilyProperty> colFamilies;
    private final ObservableList<String> excludes;

    // extra attribute
    private final DoubleProperty x;
    private final DoubleProperty y;

    public TableProperty(String name) {
        this.name = new SimpleStringProperty(name);
        this.ds = new SimpleObjectProperty<>();
        this.num = new SimpleIntegerProperty();
        this.joins = new JoinsProperty();
        this.colFamilies = new HashSet<>();
        this.excludes = FXCollections.observableArrayList();
        this.x = new SimpleDoubleProperty();
        this.y = new SimpleDoubleProperty();
    }

    private TableProperty(String name, DataSourceInfoProperty ds,
                          int num, Table.Joins joins,
                          List<Table.ColFamily> colFamilies,
                          List<String> excludes, double x, double y) {
        this.name = new SimpleStringProperty(name);
        this.ds = new SimpleObjectProperty<>(ds);
        this.num = new SimpleIntegerProperty(num);

        this.joins = new JoinsProperty(joins.getLeftJoins(),
                joins.getRightJoins());
        this.colFamilies = colFamilies.stream().map(ColFamilyProperty::map).collect(Collectors.toSet());
        this.excludes = FXCollections.observableArrayList(excludes);
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
    }

    public static TableProperty map(Table table,
                                    DataSourceInfoProperty ds, double x, double y) {
        return new TableProperty(
                table.getName(),
                ds,
                table.getNum(),
                table.getJoins(),
                table.getColFamilies(),
                table.getExcludes(), x, y);
    }

    public Table unmap() {
        return new Table(
                name.get(),
                ds.get().unmap().getName(),
                num.get(),
                joins.unmap(),
                colFamilies.stream().map(ColFamilyProperty::unmap).collect(Collectors.toList()),
                excludes
        );
    }

    public Element serial(Document document) {
        Element tableEle = document.createElement("table");
        $(tableEle)
                .attr("name", getName().get())
                .attr("ds", getDs().get() == null? "": getDs().get().getName().get())
                .attr("num", String.valueOf(getNum().get()))
                .attr("xmlns", "https://github.com/dqinyuan/fakerpp")
                .append(getJoins().serial(document))
                .append($(document.createElement("col-families"))
                        .append(
                                getColFamilies().stream()
                                        .map(cf -> cf.serial(document))
                                        .toArray(Element[]::new)
                        )
                )
                .append($(document.createElement("excludes"))
                        .append(getExcludes().stream().map(
                                ex -> $(document.createElement("exclude")).content(ex))
                                .toArray(Match[]::new)
                        )
                );

        return tableEle;
    }

}
