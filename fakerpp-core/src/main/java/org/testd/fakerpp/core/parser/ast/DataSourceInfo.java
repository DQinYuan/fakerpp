package org.testd.fakerpp.core.parser.ast;

import lombok.*;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class DataSourceInfo {
    private final String name;

    private final String type;
    private final String storer;
    private final int batchSize;
    private final String url;
    private final String user;
    private final String passwd;

    public static DataSourceInfo parse(Element element) {
        return new DataSourceInfo(
                $(element).attr("name"),
                $(element).attr("type"),
                $(element).attr("storer"),
                Integer.parseInt($(element).attr("batch-size")),
                $(element).child("url").text(),
                $(element).child("user").text(),
                $(element).child("passwd").text()
        );
    }
}
