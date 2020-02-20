import groovy.transform.BaseScript
import groovy.xml.XmlUtil
import javassist.ClassPool
import org.testd.fakerpp.core.xsdgen.Libs

import java.nio.file.Files
import java.nio.file.Paths

@BaseScript
Libs libs

def jarPath = Files.list(Paths.get("xsdgen"))
        .filter({ p -> p.getFileName().toString().startsWith("javafaker") })
        .findFirst().orElseThrow({-> new RuntimeException("can not find javafaker jar")})

ClassPool cp = ClassPool.getDefault()

// import java faker class path
cp.insertClassPath(jarPath.toAbsolutePath().toString())
// import project class path
cp.insertClassPath(Paths.get("target", "classes").toAbsolutePath().toString())

def fields = allFields(cp)

def fieldXsds = fields.collect {formatField(it)}
def regsXsd = registerFields(fields)

def parser = new XmlParser(false, false)
def fixedPartd = parser.parse(Paths.get("xsdgen", "base.xsd").toFile())


def appendXsd = {String xsd -> fixedPartd.children().add(parser.parseText(xsd))}

fieldXsds.each(appendXsd)
appendXsd(regsXsd)

Files.write(Paths.get("target", "classes", "fakerpp.xsd"),
        XmlUtil.serialize(fixedPartd).getBytes())