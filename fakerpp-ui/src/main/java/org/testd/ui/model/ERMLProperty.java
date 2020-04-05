package org.testd.ui.model;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.fakerpp.core.parser.ast.Meta;
import org.testd.fakerpp.core.parser.ast.Table;

import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ERMLProperty {

    private final MetaProperty meta;
    private final Map<String, TableProperty> tables;

    public static ERMLProperty map(ERML erml) {
        return new ERMLProperty(erml.getMeta(), erml.getTables());
    }

    private ERMLProperty(Meta meta, Map<String, Table> tables) {
        this.meta = MetaProperty.map(meta);
        Map<String, DataSourceInfoProperty> dsNameMap
                = Maps.uniqueIndex(this.meta.getDataSourceInfos(), ds -> ds.getName().get());
        this.tables = tables.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        en -> TableProperty.map(en.getValue(),
                                StringUtils.isEmpty(en.getValue().getDs())? null:
                                        dsNameMap.get(en.getValue().getDs()))
                ));
    }

}
