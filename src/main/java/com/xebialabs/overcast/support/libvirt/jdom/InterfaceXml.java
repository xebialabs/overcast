/**
 *    Copyright 2014 XebiaLabs
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

import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public final class InterfaceXml {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceXml.class);

    private InterfaceXml() {
    }

    /**
     * Get a map of mac addresses of interfaces defined on the domain. This is somewhat limited at the moment. It is
     * assumed that only one network interface with mac is connected to a bridge or network. For instance if you have a
     * bridged network device connected to 'br0' then you will find it's MAC address with the key 'br0'.
     */
    public static Map<String, String> getMacs(Document domainXml) {
        Map<String, String> macs = Maps.newHashMap();
        XPathFactory xpf = XPathFactory.instance();
        XPathExpression<Element> interfaces = xpf.compile("/domain/devices/interface", Filters.element());
        for (Element iface : interfaces.evaluate(domainXml)) {
            String interfaceType = iface.getAttribute("type").getValue();
            logger.debug("Detecting IP on network of type '{}'", interfaceType);
            if ("bridge".equals(interfaceType)) {
                Element macElement = iface.getChild("mac");
                String mac = macElement.getAttribute("address").getValue();
                Element sourceElement = iface.getChild("source");
                String bridge = sourceElement.getAttribute("bridge").getValue();
                logger.info("Detected MAC '{}' on bridge '{}'", mac, bridge);
                macs.put(bridge, mac);
            } else if ("network".equals(interfaceType)) {
                Element macElement = iface.getChild("mac");
                String mac = macElement.getAttribute("address").getValue();
                Element sourceElement = iface.getChild("source");
                String network = sourceElement.getAttribute("network").getValue();
                logger.info("Detected MAC '{}' on network '{}'", mac, network);
                macs.put(network, mac);
            } else {
                logger.warn("Ignoring network of type {}", interfaceType);
            }
        }
        return macs;
    }
}
