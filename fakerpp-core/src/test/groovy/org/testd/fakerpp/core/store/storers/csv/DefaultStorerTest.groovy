package org.testd.fakerpp.core.store.storers.csv

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.testd.fakerpp.core.parser.ast.DataSourceInfo
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class DefaultStorerTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "test temp"() {
        expect:
        println(temporaryFolder.newFile().getParent())
    }

    def "store data in `dsi.name/tableName.csv`"() {
        given:
        def dummyFile = temporaryFolder.newFile()
        def storer = new DefaultStorer()
        def dsName = dummyFile.getParent()
        def tableName = "myt"
        def csvFilePath = Paths.get(dsName, tableName + ".csv")

        when:
        def dsi = new DataSourceInfo(dsName, "", "", "", "", "")
        storer.init(dsi, 2)
        def tStorer = storer.getTableStorer(tableName, ["a", "b"])
        5.times {
            i ->
            tStorer.store(["a${i}", "b${i}"])
        }
        tStorer.flush()
        def content =
                new String(Files.readAllBytes(csvFilePath))

        then:
        content =="""a,b
a0,b0
a1,b1
a2,b2
a3,b3
a4,b4
"""
    }

}
