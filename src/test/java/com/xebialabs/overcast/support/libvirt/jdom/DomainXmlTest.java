/**
 *    Copyright 2012-2021 Digital.ai
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

import com.xebialabs.overcast.Resources;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class DomainXmlTest {

  public Document getXml(String file) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(Resources.getResource(file));
    }

    @Test
    public void shouldPrepareForCloning() throws Exception {
        Document domainXml = getXml("libvirt-xml/simple-domain.xml");

        assertThat(domainXml.getRootElement().getChild("uuid"), notNullValue());
        assertThat(domainXml.getRootElement().getChild("devices").getChild("interface").getChild("mac"), notNullValue());

        DomainXml.prepareForCloning(domainXml);

        assertThat(domainXml.getRootElement().getChild("uuid"), nullValue());
        assertThat(domainXml.getRootElement().getChild("devices").getChild("interface").getChild("mac"), nullValue());
    }
}
