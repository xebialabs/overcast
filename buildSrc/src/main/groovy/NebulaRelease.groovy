import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec

class NebulaRelease extends DefaultTask {

    @TaskAction
    void doRelease() {
        getProject().exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec execSpec) {
                def version = project.releasedVersion
                project.logger.lifecycle("Releasing version is: $version")

                execSpec.executable('./gradlew')
                execSpec.args('build', 'uploadArchives', "-Prelease.version=$version", "final",
                        "-Prelease.ignoreSuppliedVersionVerification=true")
            }
        })
    }
}
