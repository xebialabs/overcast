package com.xebialabs.overcast.support.libvirt.jdom;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

import com.google.common.io.Resources;

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
