package org.testd.fakerpp.core.store.storers

import org.testd.fakerpp.core.FakerppProperties
import org.testd.fakerpp.core.parser.ast.DataSourceInfo
import org.testd.fakerpp.core.store.storers.mocks.DiscardStorer
import org.testd.fakerpp.core.parser.ast.DataSourceInfo
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
