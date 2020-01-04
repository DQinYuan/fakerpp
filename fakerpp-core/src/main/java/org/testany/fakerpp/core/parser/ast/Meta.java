package org.testany.fakerpp.core.parser.ast;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Meta {

    private final String lang;
    private final Map<String, DataSourceInfo> dataSourceInfos;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ImmutableMap.Builder<String, DataSourceInfo> mapBuiler;
        private String lang;

        public Builder() {
            this.mapBuiler = new ImmutableMap.Builder<>();
        }

        public void lang(String lang) {
            this.lang = lang;
        }

        public void appendDataSourceInfo(DataSourceInfo info) {
            mapBuiler.put(info.getName(), info);
        }

        public Meta build() {
            return new Meta(lang, mapBuiler.build());
        }

    }



}
