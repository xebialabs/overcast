package com.xebialabs.overcast.support.libvirt.jdom;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public final class Util {
    private Util() {
    }

    public static String prettyPrint(Element element) {
        StringWriter vsw = new StringWriter();
        XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
        try {
            xout.output(element, vsw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return vsw.toString();
    }
}
