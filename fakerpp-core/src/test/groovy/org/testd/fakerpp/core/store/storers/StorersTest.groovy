package org.testd.fakerpp.core.store.storers

import org.testd.fakerpp.core.parser.ast.DataSourceInfo
import org.testd.fakerpp.core.store.storers.mocks.DiscardStorer
import spock.lang.Specification

class StorersTest extends Specification {

    def "test getInitedStorer"() {
        given:
        def storers = new Storers()
        def dsi = new DataSourceInfo(
                "mysql0",
                "mocks",
                "discard",
                100,
                "jdbc:mysql//...",
                "root",
                "123456"
        )

        when:
        def storer = storers.getInitedStorer(dsi)

        then:
        storer instanceof DiscardStorer
        storer.inited == true
    }

/*    def "test storers"() {
        expect:
        def storers = new Storers().storers()

        storers.each { type, sub ->
            println("-------")
            println(type)
            sub.each {
                name, storer ->
                    println(name)
            }
        }
    }*/
}
