package org.testd.ui.util

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
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

    static class LoProperty {
        StringProperty name
        int age

        LoProperty(String name, int age) {
            this.name = new SimpleStringProperty(name)
            this.age = age
        }

    }

    def "test map list to map with filter and key modify"() {
        given:
        ObservableList<LoProperty> list = FXCollections.observableArrayList(new LoProperty("", 1))
        Map<String, LoProperty> map = new HashMap<>()

        when:
        BindingUtil.mapContentWithFilter(map, list, {lo->lo.name}, {it},
                {it.age%2 == 0})
        def liNa = new LoProperty("LiNa", 10)
        list.add(liNa)
        def worker =  new LoProperty("Worker", 11)
        list.add(worker)
        def linlin = new LoProperty("linlin", 22)
        list.add(linlin)

        then:
        map == ["LiNa":liNa, "linlin":linlin]
        liNa.name.set("HaNa")
        map == ["HaNa":liNa, "linlin":linlin]
        list.remove(liNa)
        map == ["linlin":linlin]
    }

    def "bind map key value"() {
        given:
        Set<String> keySet = new HashSet<>()
        Set<Integer> valueSet = new HashSet<>()
        ObservableMap<String, Integer> omap = FXCollections.observableHashMap()

        when:
        BindingUtil.bindKeyValue(keySet, valueSet, omap)
        omap.put("aaa", 10)
        omap.put("bbb", 9)

        then:
        keySet == ["aaa", "bbb"] as Set
        valueSet == [10, 9] as Set

        and:
        omap.put("aaa", 100)
        omap.remove("bbb")

        then:
        keySet == ["aaa"] as Set
        valueSet == [100] as Set
    }

}
