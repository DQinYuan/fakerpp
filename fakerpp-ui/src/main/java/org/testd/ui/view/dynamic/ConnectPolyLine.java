package org.testd.ui.view.dynamic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.swing.text.TableView;
import java.util.List;

public class ConnectPolyLine {

    @Getter
    @RequiredArgsConstructor
    public static class ConnectSource {
        private final TableView table;
        private final List<ColFamilyView> joinCfs;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ConnectTarget {
        private final TableView table;
        private final List<String> targetCf;
    }

    // 连接线是如何实现跟随的

}
