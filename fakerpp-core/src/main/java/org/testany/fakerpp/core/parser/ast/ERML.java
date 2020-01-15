package org.testany.fakerpp.core.parser.ast;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ERML {

    private final Meta meta;
    private final Map<String, Table> tables;

    public static class Builder {
        private Meta meta;
        private Map<String, Table> tables;

        public Builder() {
            tables = new HashMap<>();
        }

        public Builder meta(Meta meta) {
            this.meta = meta;
            return this;
        }

        public Meta meta() {
            return this.meta;
        }

        /**
         *
         * @param table
         * @return if table duplicate
         */
        public boolean appendTable(Table table) {
            Table oldValue = this.tables.put(table.getName(), table);
            return oldValue == null;
        }

        public ERML build() {
            return new ERML(this.meta, this.tables);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
