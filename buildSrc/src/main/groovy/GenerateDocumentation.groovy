import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec

class GenerateDocumentation extends DefaultTask {

    @TaskAction
    void doRelease() {
        getProject().exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec execSpec) {
                project.logger.lifecycle("Generating documentation from markdown files")

                execSpec.executable('./gradlew')
                execSpec.args(
                        "commitChanges",
                        "-PgitBranchName=master",
                        "-PgitMessage=Documentation has been updated",
                        "-PgitFileContent=docs/*"
                )
            }
        })
    }

}
