package org.testd.fakerpp.core.parser;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * It is Singleton
 */
@Slf4j
public class FakerppSchema {

    private FakerppSchema() {}

    private static class SchemaHolder{   //利用了JVM对内部类的延迟加载
        private static Schema shchemaInstance = getScheme();

        private static Schema getScheme() {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            Schema schema = null;
            try {
                schema = schemaFactory.newSchema(FileParser.class.getResource("/fakerpp.xsd"));
            } catch (SAXException ignore) {
                log.error("fakerapp.xsd can not have error", ignore);
                throw new RuntimeException(ignore);
            }

            return schema;
        }
    }

    public static Schema getInstance() {
        return SchemaHolder.shchemaInstance;
    }

}
