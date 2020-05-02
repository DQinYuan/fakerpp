package org.testd.ui.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;

@Getter
public class DataSourceInfoProperty {
    private final StringProperty name;

    private final StringProperty type;
    private final StringProperty storer;
    private final IntegerProperty batchSize;
    private final StringProperty url;
    private final StringProperty user;
    private final StringProperty passwd;

    public DataSourceInfoProperty(DataSourceInfo dataSourceInfo) {
        this(dataSourceInfo.getName(),
                dataSourceInfo.getType(),
                dataSourceInfo.getStorer(),
                dataSourceInfo.getBatchSize(),
                dataSourceInfo.getUrl(),
                dataSourceInfo.getUser(),
                dataSourceInfo.getPasswd());
    }

    public DataSourceInfoProperty(String name, String type, String storer,
                                  int batchSize, String url, String user, String passwd) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.storer = new SimpleStringProperty(storer);
        this.batchSize = new SimpleIntegerProperty(batchSize);
        this.url = new SimpleStringProperty(url);
        this.user = new SimpleStringProperty(user);
        this.passwd = new SimpleStringProperty(passwd);
    }

    public void set(DataSourceInfo newDataSourceInfo) {
        name.set(newDataSourceInfo.getName());
        type.set(newDataSourceInfo.getType());
        storer.set(newDataSourceInfo.getStorer());
        batchSize.set(newDataSourceInfo.getBatchSize());
        url.set(newDataSourceInfo.getUrl());
        user.set(newDataSourceInfo.getUser());
        passwd.set(newDataSourceInfo.getPasswd());
    }

    public DataSourceInfo unmap() {
        return new DataSourceInfo(
                name.get(),
                type.get(),
                storer.get(),
                batchSize.get(),
                url.get(),
                user.get(),
                passwd.get()
        );
    }

    @Override
    public String toString() {
        return "DataSourceInfoProperty{" +
                "name=" + name.get() + '}';
    }
}
