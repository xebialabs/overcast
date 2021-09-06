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

import java.util.Collections;
import java.util.Map;

import com.xebialabs.overcast.Resources;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

import com.xebialabs.overcast.support.libvirt.Filesystem;
import org.junit.jupiter.api.Test;

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
