package org.testany.fakerpp.core.store.storers

import org.testany.fakerpp.core.ERMLException
import spock.lang.Specification

class BatchableTableStorerTest extends Specification {

    def "batch exec and flush"() {
        expect:
        def expect = [2, 2, 1]
        def counter = 0
        def mockTS = new BatchableTableStorer(2, ["a", "b", "c"]) {
            @Override
            Map<String, List<String>> feedBackData(List<String> excludes) throws ERMLException {
                return null
            }

            @Override
            protected void executeCommit(List<List<String>> batch) throws ERMLException {
                assert batch.size() == expect[counter++]
            }
        }
        5.times {
            mockTS.store(["a1", "b1", "c1"])
        }
        mockTS.flush()
    }

    def "invalid col number"() {
        given:
        def mockTS = new BatchableTableStorer(2, ["a", "b", "c"]) {
            @Override
            Map<String, List<String>> feedBackData(List<String> excludes) throws ERMLException {
                return null
            }

            @Override
            protected void executeCommit(List<List<String>> batch) throws ERMLException {
            }
        }

        when:
        mockTS.store(["a1", "b1"])
        mockTS.store(["a2", "b2"])

        then:
        ERMLException e = thrown()
    }

}
