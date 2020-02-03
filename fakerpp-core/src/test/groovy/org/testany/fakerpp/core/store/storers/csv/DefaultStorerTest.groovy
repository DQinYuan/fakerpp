package org.testany.fakerpp.core.store.storers.csv

import org.testany.fakerpp.core.parser.ast.DataSourceInfo
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class DefaultStorerTest extends Specification {

    def "store data in `dsi.name/tableName.csv`"() {
        given:
        def storer = new DefaultStorer()
        def dsName = "csvtest"
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
        Files.delete(csvFilePath)
        Files.delete(Paths.get(dsName))


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
