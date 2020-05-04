package org.testd.ui.view.dynamic;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.ui.vo.ColFamilyVO;
import org.testd.ui.model.ColProperty;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ColPropertyFactory {

    public Set<ColProperty> colPropertiesWithListener(Collection<String> colNames,
                                                      MyTableView owerTable) {
        return colNames.stream()
                .map(colName -> colPropertyWithListener(colName, owerTable))
                .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * create col property with delete listener,
     * which will delete corresponding col in JoinSend view
     * @param colName
     * @param owerTable
     * @return
     */
    public ColProperty colPropertyWithListener(String colName, MyTableView owerTable) {
        ColProperty colProperty = new ColProperty(colName);
        colProperty.addDeleteListener(name -> {
            // get all join send view
            List<ColFamilyVO> sendColFamilies = owerTable.getColFamilies(JoinView.class,
                    JoinView::isSend);
            sendColFamilies.forEach(colFamilyProperty -> colFamilyProperty.deleteCol(name));
        });

        return colProperty;
    }

}
