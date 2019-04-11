package com.namics.oss.gradle.version

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public open class ReleaseTask : DefaultTask() {

    init {
        group = "version"
        description = "Perform a release on master."
    }

    @TaskAction
    fun release() {
        val git = GitManager(project)
        val branch = git.branch()
        logger.info("Check if release is required on {}", branch)
        if ("master" == branch) {
            logger.info("Perform release on {}", branch)

            val version = VersionManager(project)
            version.release()?.let {
                val release = it;
                git.tag(release)

                git.checkout("develop")

                logger.info("Set version to release $release to avoid merge conflict")
                version.updateVersion(release)
                git.merge("master")

                version.snapshot()
                git.push()
                git.checkout(branch)
            }
        } else {
            logger.info("SKIP: Not on branch 'master'")
        }
    }
}
