package org.testd.fakerpp.core.engine

import org.testd.fakerpp.core.ERMLException
import org.testd.fakerpp.core.engine.domain.TableExec
import org.testd.fakerpp.core.ERMLException
import org.testd.fakerpp.core.engine.domain.TableExec
import spock.lang.Specification

class TopologySchedulerTest extends Specification {

    TableExec a
    TableExec b
    TableExec c
    TableExec d
    TableExec e

    def setup() {
        a = mockTableName("a")
        b = mockTableName("b")
        c = mockTableName("c")
        d = mockTableName("d")
        e = mockTableName("e")
    }

    def mockTableName(name) {
        def m = Mock(TableExec)
        m.getName() >> name
        return m
    }


    def mockDepends(tExec, depends) {
        tExec.getDepends() >> depends
        return tExec
    }

    def "schedule dag"() {
        given:
        /*
         a -> b -> d
              ↓    ↓
              c -> e

         */
        def tMap = [
                "a":mockDepends(a, []),
                "b":mockDepends(b, [a]),
                "c":mockDepends(c, [b]),
                "d":mockDepends(d, [b]),
                "e":mockDepends(e, [c, d])
        ]

        when:
        def scheduler = new TopologyScheduler(tMap)

        then:
        def exp = [a, b, c, d, e].iterator()
        scheduler.forEach({t ->
            assert exp.hasNext()
            assert exp.next().is(t)
        })
    }

    def "graph has cycle"() {
        given:
        /*
        a -> b -> d
             ↑    ↓
             c <- e
        */
        def tMap = [
                "a":mockDepends(a, []),
                "b":mockDepends(b, [a, c]),
                "c":mockDepends(c, [e]),
                "d":mockDepends(d, [b]),
                "e":mockDepends(e, [d])
        ]

        when:
        def scheduler = new TopologyScheduler(tMap)
        scheduler.forEach({t -> t.getName()})

        then:
        ERMLException e = thrown()
        e.getMessage() == "tables has cycle dependency"
    }

}
