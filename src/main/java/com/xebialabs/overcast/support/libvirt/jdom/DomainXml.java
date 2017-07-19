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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public final class DomainXml {
    private DomainXml() {
    }

    public static Document setDomainName(Document domainXml, String name) {
        XPathFactory xpf = XPathFactory.instance();

        XPathExpression<Element> nameExpr = xpf.compile("/domain/name", Filters.element());
        Element nameElement = nameExpr.evaluateFirst(domainXml);
        nameElement.setText(name);

        return domainXml;
    }

    /** remove elements that need to be unique per clone. */
    public static Document prepareForCloning(Document domainXml) {
        XPathFactory xpf = XPathFactory.instance();

        // remove uuid so it will be generated
        domainXml.getRootElement().removeChild("uuid");

        // remove mac address, so it will be generated
        XPathExpression<Element> macExpr = xpf.compile("/domain/devices/interface/mac", Filters.element());
        for (Element mac : macExpr.evaluate(domainXml)) {
            mac.getParentElement().removeChild("mac");
        }
        return domainXml;
    }
}
