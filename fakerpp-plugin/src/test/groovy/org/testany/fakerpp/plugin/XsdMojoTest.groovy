package org.testany.fakerpp.plugin


import org.apache.maven.plugin.testing.MojoRule
import org.junit.Rule
import spock.lang.Specification

class XsdMojoTest extends Specification {

    @Rule
    MojoRule rule = new MojoRule()

    def  "test xsd mojo"() {
        expect:
        File pom = new File( "target/test-classes/project-to-test/" )
        def mojo = rule.lookupConfiguredMojo(pom, "xsd")
        mojo.execute()
    }

}
