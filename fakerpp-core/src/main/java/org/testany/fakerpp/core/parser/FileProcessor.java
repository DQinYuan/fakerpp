package org.testany.fakerpp.core.parser;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.joox.JOOX.$;

@Slf4j
@Getter
public class FileProcessor {

    private ERML.Builder ermlBuilder;

    public FileProcessor() {
        ermlBuilder = ERML.builder();
    }

    public ERML getERML() {
        return ermlBuilder.build();
    }

    public void process(Path path) throws ERMLException {
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


    public static Document getDocument(Path xmlPath, Schema schema) throws ERMLException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setSchema(schema);
        factory.setExpandEntityReferences(false);
        // Very important to set
        factory.setNamespaceAware(true);

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlPath.toFile());
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
        Meta meta = new Meta();
        $(document)
                .child("datasources")
                .children()
                .each(ctx ->
                        meta.appendDataSourceInfo(
                                new DataSourceInfo(
                                        $(ctx).attr("name"),
                                        $(ctx).attr("type"),
                                        $(ctx).attr("storer"),
                                        $(ctx).child("url").text(),
                                        $(ctx).child("user").text(),
                                        $(ctx).child("passwd").text())
                        )
                );
        return meta;
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

    public static Table parseTableXml(Document document) {

        Table.Joins joins = parseJoins($(document).child("joins"));

        List<Table.ColFamily> colFamilies = new ArrayList<>();
        $(document).child("col-families")
                .children()
                .each(ctx -> colFamilies.add(parseColFamily($(ctx))));

        return new Table($(document).attr("name"),
                $(document).attr("ds"),
                Integer.parseInt($(document).attr("num")),
                joins,
                colFamilies,
                $(document).child("excludes").children().texts());
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

    private static final String COLS_TAG = "cols";

    private static Table.ColFamily parseColFamily(Match colFamilyCtx) {
        Match generatorTag = $(colFamilyCtx).child();
        Map<String, String> attrs = new HashMap<>();
        getAllAttrs(generatorTag)
                .forEach(attr -> attrs.put(attr.getName(), attr.getValue()));
        Map<String, List<String>> otherLists = new HashMap<>();
        $(generatorTag).children()
                .filter(othersCtx -> $(othersCtx).tag() != COLS_TAG)
                .each(othersCtx -> otherLists.put($(othersCtx).tag(),
                        $(othersCtx).children().texts())
                );

        return new Table.ColFamily($(generatorTag)
                .find(COLS_TAG)
                .children()
                .texts(),
                $(colFamilyCtx).tag(),
                generatorTag.tag(),
                attrs,
                otherLists);

    }

}
