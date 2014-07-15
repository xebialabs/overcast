package com.xebialabs.overcast.support.libvirt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public final class JDomUtil {
    private JDomUtil() {
    }

    public static String getElementText(Element parent, String localName, Namespace ns) {
        if (parent == null) {
            throw new IllegalArgumentException("parent element not found");
        }
        Element child = parent.getChild(localName, ns);
        if (child == null) {
            throw new IllegalArgumentException(String.format("child element '%s' not found", localName));
        }
        return child.getText();
    }

    /** Convert xml to JDOM2 {@link Document}. Throws {@link IllegalArgumentException} in case of errors. */
    public static Document stringToDocument(String xml) {
        try {
            SAXBuilder sax = new SAXBuilder();
            return sax.build(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        } catch (JDOMException e) {
            throw new IllegalArgumentException("Unable to parse xml", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to parse xml", e);
        }
    }

    public static String documentToString(Document xml, Format format) throws IOException {
        StringWriter vsw = new StringWriter();
        XMLOutputter xout = new XMLOutputter(format);
        xout.output(xml, vsw);
        return vsw.toString();
    }

    public static String documentToString(Document xml) throws IOException {
        return documentToString(xml, Format.getPrettyFormat());
    }

    public static String documentToRawString(Document xml) throws IOException {
        return documentToString(xml, Format.getRawFormat().setOmitDeclaration(true)).trim();
    }

    public static String elementToString(Element element, Format format) throws IOException {
        StringWriter vsw = new StringWriter();
        XMLOutputter xout = new XMLOutputter(format);
        xout.output(element, vsw);
        return vsw.toString();
    }

    public static String elementToString(Element element) throws IOException {
        return elementToString(element, Format.getPrettyFormat());
    }
}
