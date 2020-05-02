package org.testd.ui.util;

import com.google.common.io.CharSource;
import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Consumer;

public class XmlUtil {

    public static Document newDocument() {
        try {
            return DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * pretty serialize
     * @param document
     * @return
     */
    public static String serialize(Document document) {
        return serialize(document, tran -> {
            tran.setOutputProperty(OutputKeys.INDENT, "yes");
            tran.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        });
    }

    public static String serialize(Document document, Consumer<Transformer> transformerConfig) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformerConfig.accept(transformer);

            DOMSource domSource = new DOMSource(document);
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            transformer.transform(domSource, streamResult);
            return stringWriter.getBuffer().toString();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static String genXml(Consumer<Document> constructor) {
        Document document = newDocument();
        constructor.accept(document);
        return serialize(document);
    }

    public static Element rootElement(String xmlContent) throws SAXException {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            return documentBuilder.parse(new ByteArrayInputStream(xmlContent.getBytes())).getDocumentElement();
        } catch (ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }


}
