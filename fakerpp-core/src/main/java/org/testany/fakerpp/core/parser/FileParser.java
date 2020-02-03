package org.testany.fakerpp.core.parser;

import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joox.Match;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.parser.ast.DataSourceInfo;
import org.testany.fakerpp.core.parser.ast.ERML;
import org.testany.fakerpp.core.parser.ast.Meta;
import org.testany.fakerpp.core.parser.ast.Table;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.joox.JOOX.$;

@Slf4j
@Getter
public class FileParser {

    private ERML.Builder ermlBuilder;

    public FileParser() {
        ermlBuilder = ERML.builder();
    }

    public ERML getERML() {
        return ermlBuilder.build();
    }

    public void processByDir(Path path) throws ERMLException {
        // meta directory
        if (Files.isDirectory(path) && path.endsWith("meta")) {
            if (ermlBuilder.meta() == null) {
                Document doc = getDocument(path.resolve("meta.xml"), MetaSchema.getInstance());
                ermlBuilder.meta(parseMetaXml(doc));
            }
            return;
        }

        // normal xml
        if (!Files.isDirectory(path) && path.getFileName().toString().endsWith(".xml")) {
            ermlBuilder.appendTable(parseTableXml(getDocument(path, FakerppSchema.getInstance())));
            return;
        }
    }

    public void processByStream(InputStream metaStream, List<InputStream> tableStreams) throws ERMLException {
        ermlBuilder.meta(parseMetaXml(getDocument(metaStream, MetaSchema.getInstance())));
        for (InputStream tableStream : tableStreams) {
            ermlBuilder.appendTable(parseTableXml(getDocument(tableStream,
                    FakerppSchema.getInstance())));
        }
    }


    public static Document getDocument(Path xmlPath, Schema schema) throws ERMLException {
        try {
            return getDocument(new FileInputStream(xmlPath.toFile()), schema);
        } catch (FileNotFoundException e) {
            throw new ERMLException("xml file not found", e);
        }
    }

    public static Document getDocument(InputStream xmlContent, Schema schema) throws ERMLException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setSchema(schema);
        factory.setExpandEntityReferences(false);
        // Very important to set
        factory.setNamespaceAware(true);

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlContent);
            return doc;
        } catch (ParserConfigurationException ignore) {
            log.error("document buld error", ignore);
            throw new RuntimeException(ignore);
        } catch (SAXException e) {
            throw new ERMLException("xml parse error", e);
        } catch (IOException e) {
            throw new ERMLException("read file error", e);
        }
    }


    public static Meta parseMetaXml(Document document) {
        Meta.Builder builder = Meta.builder();
        $(document)
                .child("datasources")
                .children()
                .each(ctx ->
                        builder.appendDataSourceInfo(
                                new DataSourceInfo(
                                        $(ctx).attr("name"),
                                        $(ctx).attr("type"),
                                        $(ctx).attr("storer"),
                                        $(ctx).child("url").text(),
                                        $(ctx).child("user").text(),
                                        $(ctx).child("passwd").text())
                        )
                );
        builder.lang($(document).attr("lang"));
        return builder.build();
    }

    public static Table.Joins parseJoins(Match joinsCtx) {
        List<Table.Join> lefts = new ArrayList<>();
        List<Table.Join> rights = new ArrayList<>();
        $(joinsCtx)
                .children()
                .each(ctx -> {
                            Map<String, String> map = new HashMap<>();
                            $(ctx).children("map")
                                    .each(mapCtx ->
                                    {
                                        String from = $(mapCtx).attr("from");
                                        String to = $(mapCtx).text().trim();
                                        if (StringUtils.isEmpty(to)) {
                                            map.put(from, from);
                                        } else {
                                            map.put(from, to);
                                        }
                                    });
                            switch ($(ctx).tag()) {
                                case "leftjoin":
                                    lefts.add(new Table.Join(map, $(ctx).attr("depend"), false));
                                    break;
                                case "rightjoin":
                                    rights.add(new Table.Join(map, $(ctx).attr("depend"),
                                            Boolean.parseBoolean($(ctx).attr("random"))
                                    ));
                                    break;
                            }
                        }
                );
        return new Table.Joins(lefts, rights);
    }

    public static Table parseTableXml(Document document) throws ERMLException {

        Table.Joins joins = parseJoins($(document).child("joins"));

        List<Table.ColFamily> colFamilies = new ArrayList<>();
        $(document).child("col-families")
                .children()
                .each(ctx -> colFamilies.add(parseColFamily($(ctx))));

        String tableName = $(document).attr("name");
        // joox will return null if this attr not exists
        String dataSource = Strings.nullToEmpty($(document).attr("ds"));
        List<String> excludes = $(document).child("excludes").children().texts();
        // virtual table can not have excludes
        if ("".equals(dataSource) && excludes.size() > 0) {
            throw new ERMLException(
                    String.format("virtual table %s can not have <excludes> tag",
                            tableName)
            );
        }

        return new Table(tableName,
                dataSource,
                Integer.parseInt($(document).attr("num")),
                joins,
                colFamilies,
                excludes);
    }

    @Getter
    @Builder
    private static class Attr {
        private String name;
        private String value;
    }

    private static Stream<Attr> getAllAttrs(Match genTag) {
        List<Attr> attrs = new ArrayList<>();
        Element element = $(genTag).get(0);
        NamedNodeMap attributes = element.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            attrs.add(Attr.builder()
                    .name(attributes.item(i).getLocalName())
                    .value(attributes.item(i).getTextContent()).build());
        }

        return attrs.stream();
    }

    private static final String OPTIONS_TAG = "options";
    private static final String COLS_TAG = "cols";

    private static Table.ColFamily parseColFamily(Match colFamilyCtx) {
        if ("composes".equals(colFamilyCtx.tag())) {
            ArrayList<Table.GeneratorInfo> generatorInfos = new ArrayList<>();
            $(colFamilyCtx).children("compose")
                    .each(
                            composeCtx ->
                                    generatorInfos.add(parseGenerator($(composeCtx).child(),
                                            Integer.parseInt($(composeCtx).attr("weight")))
                                    )

                    );
            return new Table.ColFamily($(colFamilyCtx).child(COLS_TAG).texts(),
                    generatorInfos);
        }

        return new Table.ColFamily($(colFamilyCtx).child()
                .child(COLS_TAG)
                .children()
                .texts(),
                Arrays.asList(parseGenerator(colFamilyCtx, 1))
        );
    }

    private static Table.GeneratorInfo parseGenerator(Match generatorCtx, int weight) {
        Match generatorTag = $(generatorCtx).child();

        Map<String, String> attrs = new HashMap<>();
        getAllAttrs(generatorTag)
                .forEach(attr -> attrs.put(attr.getName(), attr.getValue()));

        List<List<String>> options = new ArrayList<>();
        $(generatorTag).child(OPTIONS_TAG)
                .children()
                .each(optionCtx ->
                        options.add($(optionCtx).children("cell").texts())
                );

        return new Table.GeneratorInfo(
                $(generatorCtx).tag(),
                Strings.nullToEmpty($(generatorCtx).attr("lang")),
                generatorTag.tag(),
                attrs,
                options,
                weight
        );
    }

}
