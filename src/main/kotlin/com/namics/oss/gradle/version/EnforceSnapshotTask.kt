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
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

public open class EnforceSnapshotTask : DefaultTask() {

    @Input
    var majorBranches: List<Regex> = emptyList()
    @Input
    var minorBranches: List<Regex> = listOf(Regex("""^develop.*"""))
    @Input
    var patchBranches: List<Regex> = listOf(Regex("""^hotfix.*"""))
    @Input
    val git : GitManager = NativeGitManager(project)

    init {
        group = "version"
        description = "Development branches like develop, release*, hotfix* shall represent SNAPSHOT versions. This task enforces this constraint"
    }

    @TaskAction
    fun release() {
        logger.info("Check requirement of SNAPSHOT version")
        if (!project.version.toString().endsWith("-SNAPSHOT")) {
            val snapshot = VersionManager(project, majorBranches, minorBranches, patchBranches, git).snapshot()
            git.push()
            throw GradleException("${project.version} is not a SNAPSHOT version"
                    + "\nBranch '${git.branch()}' must represent a SNAPSHOT version"
                    + "\nVersion was set to $snapshot, next build should succeed!")
        }
    }
}
