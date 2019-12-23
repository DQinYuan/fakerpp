package org.testany.fakerpp.core.parser.ast;

import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class DataSourceInfo {
    private final String name;
    private final String type;
    private final String storer;
    private final String url;
    private final String user;
    private final String passwd;
}
