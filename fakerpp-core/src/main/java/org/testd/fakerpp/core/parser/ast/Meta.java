package org.testd.fakerpp.core.parser.ast;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.joox.JOOX.$;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Meta {

    public static final Path metaDir = Paths.get("meta");

    public static final Path metaXmlPath = metaDir.resolve("meta.xml");

    private final String lang;
    private final Map<String, DataSourceInfo> dataSourceInfos;

    public static Map<String, DataSourceInfo> parseDataSources(Element element) {
        ImmutableMap.Builder<String, DataSourceInfo> dataSourceInfosBuilder
                = ImmutableMap.builder();
        $(element)
                .children()
                .each(ctx ->
                        {
                            DataSourceInfo dataSourceInfo = DataSourceInfo.parse(ctx.element());
                            dataSourceInfosBuilder.put(dataSourceInfo.getName(),
                                    dataSourceInfo);
                        }
                );

        return dataSourceInfosBuilder.build();
    }

    public static Meta parseMeta(Element element) {
        return new Meta($(element).attr("lang"),
                parseDataSources($(element).child("datasources").get(0)));
    }


}
