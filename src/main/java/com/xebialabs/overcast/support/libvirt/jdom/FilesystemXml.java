package com.xebialabs.overcast.support.libvirt.jdom;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import com.xebialabs.overcast.support.libvirt.Filesystem;
import com.xebialabs.overcast.support.libvirt.Filesystem.AccessMode;

public final class FilesystemXml {
    private static Logger logger = LoggerFactory.getLogger(FilesystemXml.class);

    private static final String XPATH_FILESYSTEM = "/domain/devices/filesystem[@type='mount']";

    private FilesystemXml() {
    }

    public static Element toFileSystemXml(Filesystem fs) {
        Element filesystem = new Element("filesystem")
            .setAttribute("type", "mount")
            .setAttribute("accessmode", fs.accessMode.name().toLowerCase());

        filesystem.addContent(new Element("source").setAttribute("dir", fs.source));
        filesystem.addContent(new Element("target").setAttribute("dir", fs.target));
        if (fs.readOnly) {
            filesystem.addContent(new Element("readonly"));
        }
        return filesystem;
    }

    public static void removeFilesystemsWithTarget(Document domainXml, String targetDir) {
        XPathFactory xpf = XPathFactory.instance();
        XPathExpression<Element> fsExpr = xpf.compile(String.format("/domain/devices/filesystem[@type='mount']/target[@dir='%s']", targetDir), Filters.element());
        List<Element> tfs = fsExpr.evaluate(domainXml);
        for (Element e : tfs) {
            e.getParentElement().getParentElement().removeContent(e.getParentElement());
        }
    }

    /**
     * Get map of {@link Filesystem}s. The key in the map is the target inside the domain. This will only return
     * filesystems of type 'mount'.
     */
    public static Map<String, Filesystem> getFilesystems(Document domainXml) {
        Map<String, Filesystem> ret = Maps.newHashMap();
        XPathFactory xpf = XPathFactory.instance();
        XPathExpression<Element> fsExpr = xpf.compile(XPATH_FILESYSTEM, Filters.element());
        List<Element> filesystems = fsExpr.evaluate(domainXml);
        for (Element fs : filesystems) {
            logger.debug("Found existing filesystem: {}", Util.prettyPrint(fs));
            Attribute accessMode = fs.getAttribute("accessmode");
            String source = fs.getChild("source").getAttribute("dir").getValue();
            String target = fs.getChild("target").getAttribute("dir").getValue();
            boolean readOnly = fs.getChild("readonly") != null;

            ret.put(target, new Filesystem(source, target, AccessMode.valueOf(accessMode.getValue().toUpperCase(Locale.US)), readOnly));
        }
        return ret;
    }
}
