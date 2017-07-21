package com.xebialabs.overcast.gradle;

import groovy.json.JsonSlurper;
import org.gradle.api.Project;

public class InstanceUtil {

    static Map<String, Map<String, String>> readInstances(Project project) {
        def instanceFile = new File(project.getBuildDir(), "overcast/instances.json")
        if(instanceFile.exists()) {
            return new JsonSlurper().parseText(instanceFile.text)
        } else {
            return [:]
        }
    }
}
