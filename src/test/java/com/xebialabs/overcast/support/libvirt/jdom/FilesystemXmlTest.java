package com.xebialabs.overcast.support.libvirt.jdom;

import java.util.Collections;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

import com.google.common.io.Resources;

import com.xebialabs.overcast.support.libvirt.Filesystem;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;

public class FilesystemXmlTest {

    public Document getXml(String file) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(Resources.getResource(file));
    }

    @Test
    public void shouldReadNoFilesystems() throws Exception {
        Document domainXml = getXml("libvirt-xml/simple-domain.xml");
        Map<String, Filesystem> fs = FilesystemXml.getFilesystems(domainXml);
        assertThat(fs, equalTo(Collections.EMPTY_MAP));
    }

    @Test
    public void shouldReadFilesystems() throws Exception {
        Document domainXml = getXml("libvirt-xml/domain-with-filesystem.xml");

        Map<String, Filesystem> fs = FilesystemXml.getFilesystems(domainXml);
        assertThat(fs.keySet(), hasSize(2));

        assertThat(fs, hasKey("/vagrant"));
        assertThat(fs.get("/vagrant").readOnly, equalTo(true));

        assertThat(fs, hasKey("/data"));
        assertThat(fs.get("/data").readOnly, equalTo(false));
        assertThat(fs.get("/data").source, equalTo("/mnt/data"));
    }
}
