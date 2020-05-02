package org.testd.ui.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.joox.Match;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.fakerpp.core.parser.ast.Meta;
import org.testd.fakerpp.core.parser.ast.Table;
import org.testd.ui.util.XmlUtil;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.joox.JOOX.$;

@Getter
public class ERMLProperty {

    public static final Path posesXmlPath = Meta.metaDir.resolve("poses.xml");

    private final MetaProperty meta;
    private final Set<TableProperty> tables;

    public static ERMLProperty map(ERML erml) {
        return new ERMLProperty(erml.getMeta(), erml.getTables());
    }

    public ERML unmap() {
        return new ERML(
                meta.unmap(),
                tables.stream().collect(Collectors.toMap(
                        tp -> tp.getName().get(),
                        TableProperty::unmap
                ))
        );
    }

    private ERMLProperty(Meta meta, Map<String, Table> tables) {
        this.meta = MetaProperty.map(meta);
        Map<String, DataSourceInfoProperty> dsNameMap
                = Maps.uniqueIndex(this.meta.getDataSourceInfos(), ds -> ds.getName().get());

        this.tables = Streams.mapWithIndex(tables.values().stream(),
                (table, index) -> TableProperty.map(
                        table,
                        StringUtils.isEmpty(table.getDs()) ? null : dsNameMap.get(table.getDs()),
                        index * 300.0, 0
                )).collect(Collectors.toSet());
    }

    /**
     * @param xmlDumper {@code <file relative path, file content>}
     */
    public void serial(BiConsumer<Path, String> xmlDumper) {
        xmlDumper.accept(Meta.metaXmlPath, XmlUtil.genXml(
                doc -> doc.appendChild(meta.serial(doc))
        ));

        tables.forEach(table ->
                xmlDumper.accept(Paths.get(String.format("%s.xml", table.getName().get())),
                        XmlUtil.genXml(
                                doc -> doc.appendChild(table.serial(doc))
                        )
                )
        );

        xmlDumper.accept(posesXmlPath, XmlUtil.genXml(
                posDoc -> {
                    Element posRoot = posDoc.createElement("poses");
                    $(posRoot).append(
                            tables.stream().map(tp ->
                                    $(posDoc.createElement("pos"))
                                            .attr("table", tp.getName().get())
                                            .attr("x", String.valueOf(tp.getX().get()))
                                            .attr("y", String.valueOf(tp.getY().get()))
                            ).toArray(Match[]::new)
                    );
                    posDoc.appendChild(posRoot);
                }
        ));
    }

    public void updatePoses(Map<String, Pair<Double, Double>> poses) {
        tables.forEach(tableProperty -> {
            if (poses.containsKey(tableProperty.getName().get())) {
                Pair<Double, Double> pos = poses.get(tableProperty.getName().get());
                tableProperty.getX().set(pos.getValue0());
                tableProperty.getY().set(pos.getValue1());
            }
        });
    }

}
