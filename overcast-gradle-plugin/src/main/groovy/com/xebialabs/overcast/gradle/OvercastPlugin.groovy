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
package com.xebialabs.overcast.gradle

import com.xebialabs.overcast.gradle.tasks.OvercastSetup
import com.xebialabs.overcast.gradle.tasks.OvercastTeardown
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

public class OvercastPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        def extension = project.extensions.create("overcast", OvercastPluginExtension)

        project.getLogger().lifecycle("Overcast plugin applied GROOVY ");
        project.tasks.withType(Test) { task ->
            task.doFirst {
                def instances = InstanceUtil.readInstances(project)
                instances.each {
                    def overcastKey = "overcast-" + it.key
                    def overcastValue = it.value["hostname"]
                    project.getLogger().info("Setting overcast system properties: ${overcastKey} = ${overcastValue}")
                    task.systemProperties[overcastKey] = overcastValue
                }
            }
        }


        project.tasks.create("overcastSetup", OvercastSetup).configure {
            conventionMapping.labels = { -> extension.labels }
        }

        project.tasks.withType(Test){ task ->
            task.mustRunAfter 'overcastSetup'
        }

        project.tasks.create("overcastTeardown", OvercastTeardown).configure {
          outputs.upToDateWhen { false }
        }.mustRunAfter project.tasks.withType(Test)

    }

}
