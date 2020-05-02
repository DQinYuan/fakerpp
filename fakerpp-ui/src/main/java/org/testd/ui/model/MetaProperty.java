package org.testd.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.parser.ast.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.stream.Collectors;

import static org.joox.JOOX.$;

@Getter
public class MetaProperty {

    private final StringProperty lang;
    private final ObservableList<DataSourceInfoProperty> dataSourceInfos;

    private MetaProperty(String lang, Map<String, DataSourceInfo> dataSourceInfos) {
        this.lang = new SimpleStringProperty(lang);
        this.dataSourceInfos = dataSourceInfos.values().stream()
                .map(DataSourceInfoProperty::new)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static MetaProperty map(Meta meta) {
        return new MetaProperty(meta.getLang(),
                meta.getDataSourceInfos());
    }

    public Meta unmap() {
        return new Meta(
                lang.get(),
                dataSourceInfos.stream().collect(
                        Collectors.toMap(
                                property -> property.getName().get(),
                                DataSourceInfoProperty::unmap
                        )
                )
        );
    }

    public Element serialDataSources(Document document) {
        Element dataSources = document.createElement("datasources");
        dataSourceInfos.forEach(
                dataSourceInfoProperty ->
                        $(dataSources).append(
                                $(document.createElement("datasource"))
                                        .attr("name", dataSourceInfoProperty.getName().get())
                                        .attr("type", dataSourceInfoProperty.getType().get())
                                        .attr("storer", dataSourceInfoProperty.getStorer().get())
                                        .attr("batch-size",
                                                String.valueOf(dataSourceInfoProperty.getBatchSize().get())
                                        )
                                        .append(
                                                $(document.createElement("url"))
                                                        .content(dataSourceInfoProperty.getUrl().get())

                                        )
                                        .append(
                                                $(document.createElement("user"))
                                                        .content(dataSourceInfoProperty.getUser().get())
                                        )
                                        .append(
                                                $(document.createElement("passwd"))
                                                        .content(dataSourceInfoProperty.getPasswd().get())
                                        )
                        )
        );

        return dataSources;
    }

    public Element serial(Document document) {
        Element meta = document.createElement("meta");
        $(meta).attr("lang", lang.get())
                .attr("xmlns", "https://github.com/dqinyuan/fakerpp/meta")
                .append(serialDataSources(document));
        return meta;
    }
}
