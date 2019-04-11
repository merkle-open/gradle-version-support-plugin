package com.namics.oss.gradle.version

import org.gradle.api.Project
import java.io.File
import java.util.*

/*
 * Copyright 2000-2019 Namics AG. All rights reserved.
 */
class VersionManager(private val project: Project) {

    private val versionFile = File(project.projectDir, "gradle.properties")
    private val git = GitManager(project)

    fun snapshot(): SemVer? {
        val current = SemVer.parse(currentVersion())
        val branch = git.branch()
        if (current.isRelease()) {
            if (branch.matches(Regex("""^develop"""))) {
                val version = SemVer(current.major, current.minor + 1, 0, "SNAPSHOT")
                return updateVersion(version)
            }
            else if (branch.matches(Regex("""^hotfix/.*"""))) {
                val version = SemVer(current.major, current.minor, current.patch + 1, "SNAPSHOT")
                return updateVersion(version)
            }
        }
        project.logger.info("SKIP: no adjustment for version $current on branch $branch")
        return null
    }

    fun release(): SemVer? {
        val current = SemVer.parse(currentVersion())
        if (!current.isRelease()) {
            val version = SemVer(current.major, current.minor, current.patch)
            return updateVersion(version)
        } else {
            project.logger.info("SKIP: release of $current is already a release version")
            return null
        }
    }


    fun updateVersion(version: SemVer) : SemVer?{
        project.logger.info("Update to $version")
        val key = "version"
        val temp = createTempFile()
        temp.deleteOnExit()
        temp.printWriter().use { writer ->
            versionFile.forEachLine { line ->
                writer.println(when {
                    Regex("""^$key=.*$""").matches(line) -> "$key=$version"
                    else -> line
                })
            }
        }
        temp.copyTo(versionFile, true)
        git.add(versionFile)
        git.commit("Update version to ${currentVersion()}")
        return version
    }


    fun currentVersion() : String {
        val properties = Properties()
        properties.load(versionFile.inputStream())
        return properties.getProperty("version")
    }

}
