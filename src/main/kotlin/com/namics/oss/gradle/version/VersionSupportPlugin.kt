package com.namics.oss.gradle.version

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

public class VersionSupportPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        tasks.register("enforceSnapshotOnBranch", EnforceSnapshotTask::class)
        tasks.register("release", ReleaseTask::class)
    }

}
