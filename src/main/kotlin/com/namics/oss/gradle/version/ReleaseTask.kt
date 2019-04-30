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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

public open class ReleaseTask : DefaultTask() {

    @Input
    var masterBranch = "master"
    @Input
    var developBranch = "develop"
    @Input
    var majorBranches: List<Regex> = emptyList()
    @Input
    var minorBranches: List<Regex> = listOf(Regex("""^develop.*"""))
    @Input
    var patchBranches: List<Regex> = listOf(Regex("""^hotfix.*"""))

    var git = GitManager(project)
    var versionManager = VersionManager(project, majorBranches, minorBranches, patchBranches)

    init {
        group = "version"
        description = "Perform a release on master."
    }

    @TaskAction
    fun release() {
        val branch = git.branch()
        logger.info("Check if release is required on {}", branch)
        if (masterBranch == branch) {
            logger.info("Perform release on {}", branch)
            val current = SemVer.parse(versionManager.currentVersion())
            if (!current.isRelease()) {
                val newVersion =  SemVer(current.major, current.minor, current.patch)
                val release = versionManager.updateVersion(newVersion)
                git.add(".")
                git.commit("Update version to $release")
                git.tag(release)
                git.checkout(developBranch)
                logger.info("Set version to release $release to avoid merge conflict")
                versionManager.updateVersion(release)
                git.merge(masterBranch)
                versionManager.snapshot()
                git.push()
                git.checkout(branch) // checkout previous master branch to restore local state (popd)
            } else
                logger.warn("SKIP: Branch '${masterBranch}' already on release version $current")
        } else {
            logger.info("SKIP: Not on branch '$masterBranch'")
        }
    }
}
