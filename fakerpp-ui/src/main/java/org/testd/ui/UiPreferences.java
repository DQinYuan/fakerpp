package org.testd.ui;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.parser.ast.Meta;
import org.testd.ui.model.MetaProperty;
import org.testd.ui.util.XmlUtil;
import org.xml.sax.SAXException;

import java.util.Map;
import java.util.prefs.Preferences;

@Component
public class UiPreferences {

    private MetaProperty metaProperty;
    private final Preferences pref =
            Preferences.userNodeForPackage(getClass());

    public static final String dataSourcesKey = "dataSources";
    public static final String workSpaceKey = "workspace";

    public void put(String key, String content) {
        pref.put(key, content);
    }

    /**
     *
     * @return null if the pref not exists
     */
    public String get(String key) {
        return pref.get(key, null);
    }

    public Map<String, DataSourceInfo> getDataSources() {
        try {
            return get(UiPreferences.dataSourcesKey) == null ? ImmutableMap.of() :
                    Meta.parseDataSources(XmlUtil.rootElement(get(UiPreferences.dataSourcesKey)));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
