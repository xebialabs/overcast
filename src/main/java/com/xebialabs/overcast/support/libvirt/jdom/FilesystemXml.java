/**
 *    Copyright 2012-2017 XebiaLabs B.V.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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

import com.google.common.collect.Maps;

import com.xebialabs.overcast.support.libvirt.Filesystem;
import com.xebialabs.overcast.support.libvirt.Filesystem.AccessMode;

public final class FilesystemXml {
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
            Attribute accessMode = fs.getAttribute("accessmode");
            String source = fs.getChild("source").getAttribute("dir").getValue();
            String target = fs.getChild("target").getAttribute("dir").getValue();
            boolean readOnly = fs.getChild("readonly") != null;

            ret.put(target, new Filesystem(source, target, AccessMode.valueOf(accessMode.getValue().toUpperCase(Locale.US)), readOnly));
        }
        return ret;
    }
}
