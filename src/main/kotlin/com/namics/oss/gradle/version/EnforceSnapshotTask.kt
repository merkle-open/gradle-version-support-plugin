package com.namics.oss.gradle.version

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

public open class EnforceSnapshotTask : DefaultTask() {

    init {
        group = "version"
        description = "Development branches like develop, release*, hotfix* shall represent SNAPSHOT versions. This task enforces this constraint"
    }

    @TaskAction
    fun release(){
        if (!project.version.toString().endsWith("-SNAPSHOT")) {
            val snapshot = VersionManager(project).snapshot()
            GitManager(project).push()

            throw GradleException("${project.version} is not a SNAPSHOT version"
                    + "\nBranch '${GitManager(project).branch()}' must represent a SNAPSHOT version"
                    + "\nVersion was set to $snapshot, next build should succeed!")
        }
    }
}
