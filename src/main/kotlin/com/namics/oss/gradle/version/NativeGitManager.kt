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

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.IOException
import java.lang.ProcessBuilder.Redirect.PIPE
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.streams.asSequence

public class NativeGitManager(private val project: Project) : GitManager {

    val root = project.projectDir;
    val timoutSeconds = 120L
    val logger = project.logger
    val skipPush = "true" == project.findProperty("skipPush")

    override fun branch(): String = perform("branch", "--no-color")
            .filter { it.startsWith("*") }
            .findFirst()
            .map { it.replace(Regex("""\*|\s+"""), "") }
            .get()

    private fun status(): Stream<String> = perform("status")

    override fun add(file: String) = git("add", file)

    override fun commit(message: String) = git("commit", "-m", "[Bot] $message")

    override fun tag(version: SemVer) = git("tag", "-a", version.toString(), "-m", "[Bot] Release $version: create tag")

    private fun checkoutRemote(branch: String) = git("checkout", "-f", "-B", branch, "origin/$branch")

    override fun checkout(branch: String) {
        try {
            info("Try checkout local")
            git("checkout", branch)
        } catch (e: GradleException) {
            info("Failed. Try checkout remote")
            checkoutRemote(branch);
        }
    }

    override fun merge(branch: String) = git("merge", branch, "--no-edit", "-m", "[Bot] merge $branch")

    override fun push() {
        if (skipPush) return
        git("push", "--all")
        git("push", "--tags")
    }

    private fun git(vararg arguments: String) {
        if (logger.isInfoEnabled) {
            info("On branch ${branch()}")
            info("git ${arguments.joinToString(separator = " ")}")
            status().forEach { info(it) }
        }
        val output = perform(*arguments)
        if (logger.isInfoEnabled)
            output.asSequence().forEach { info(it) }
    }

    private fun info(message: String) {
        logger.info("GIT: {}", message)
    }

    private fun perform(vararg arguments: String): Stream<String> {
        try {
            val process = ProcessBuilder("git", *arguments)
                    .directory(root)
                    .redirectOutput(PIPE)
                    .redirectError(PIPE)
                    .start()

            process.waitFor(timoutSeconds, TimeUnit.SECONDS)
            val errors = ArrayList<String>()
            process.errorStream.bufferedReader().lines().forEach {
                logger.error("GIT: {}", it)
                errors.add(it)
            }

            val exitValue = process.exitValue()
            if (exitValue != 0)
                throw GradleException("exit $exitValue; \nCommand 'git ${arguments.joinToString(separator = " ")}' failed!\n${errors.joinToString("\n")}")

            return process.inputStream.bufferedReader().lines()
        } catch (e: IOException) {
            throw GradleException("Failed to execute command 'git ${arguments.joinToString(separator = " ")}'", e)
        }
    }
}
