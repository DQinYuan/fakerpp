package org.testany.fakerpp.core.parser.ast;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
public class Meta {

    private Map<String, DataSourceInfo> dataSourceInfos;

    public Meta() {
        dataSourceInfos = new HashMap<>();
    }

    public boolean appendDataSourceInfo(DataSourceInfo info) {
        DataSourceInfo oldValue = dataSourceInfos.put(info.getName(), info);
        return oldValue == null;
    }
}
