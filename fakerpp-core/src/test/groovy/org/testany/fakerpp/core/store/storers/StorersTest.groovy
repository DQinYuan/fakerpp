package org.testany.fakerpp.core.store.storers

import org.testany.fakerpp.core.FakerppProperties
import org.testany.fakerpp.core.parser.ast.DataSourceInfo
import org.testany.fakerpp.core.store.storers.mocks.DiscardStorer
import spock.lang.Specification

class StorersTest extends Specification {

    static Storers getStorers(int batchSize=10) {
        def prop = new FakerppProperties()
        prop.store = new FakerppProperties.Store()
        prop.store.batchSize = batchSize
        return new Storers(prop)
    }

    def "test getInitedStorer"() {
        given:
        def storers = getStorers()
        def dsi = new DataSourceInfo(
                "mysql0",
                "mocks",
                "discard",
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
}
