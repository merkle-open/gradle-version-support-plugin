/**
 * MIT License
 *
 * Copyright (c) 2019 Namics AG
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
