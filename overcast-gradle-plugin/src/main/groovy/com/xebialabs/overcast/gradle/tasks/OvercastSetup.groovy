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
package com.xebialabs.overcast.gradle.tasks

import com.xebialabs.overcast.cli.OvercastCli
import groovy.json.JsonOutput;
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction

public class OvercastSetup extends DefaultTask {

    @Input
    public File config;

    @Input
    public List<String> labels;

    public Map<String, Map<String, String>> instances = new HashMap<>()

    @TaskAction
    void setup() {
        this.getProject().getLogger().lifecycle("Setting up instances: " + labels)
        OvercastCli cli = new OvercastCli()
        instances = cli.setup(labels)
        writeInstances(instances)
    }

    private void writeInstances(Map<String, Map<String, String>> instances) {
        def json = JsonOutput.toJson(instances)
        def instanceFile = new File(getProject().getBuildDir(), "overcast/instances.json")
        if(instanceFile.exists()) {
            instanceFile.delete()
        }
        instanceFile.getParentFile().mkdirs()
        instanceFile.createNewFile()
        instanceFile.write(json)

    }


}
