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
