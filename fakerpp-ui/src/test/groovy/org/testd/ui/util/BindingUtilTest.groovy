package org.testd.ui.util

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import spock.lang.Specification

class BindingUtilTest extends Specification {

    static class Lo {
        String name

        Lo(String name) {
            this.name = name
        }


        @Override
        String toString() {
            return name
        }
    }

    def "test map set to list"() {
        given:
        ObservableList<Lo> list = FXCollections.observableArrayList()
        FXCollections.observableSet()
        ObservableSet<String> set =
                FXCollections.observableSet(["a", "bbb", "c", "lm"] as Set)
        BindingUtil.mapContent(list, set, {new Lo(it)},
                {i1, i2 -> i1.name == i2.name})

        when:
        set.add("xxx")
        set.remove("bbb")

        then:
        list*.toString() == ["a", "c", "lm", "xxx"]
    }

}
