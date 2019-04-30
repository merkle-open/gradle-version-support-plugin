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
import java.io.File
import java.io.IOException
import java.lang.ProcessBuilder.Redirect.PIPE
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.streams.asSequence
import kotlin.streams.asStream

class GitManager(private val project: Project) {

    val root = project.projectDir;
    val timoutSeconds = 120L
    val logger = project.logger
    val skipPush = "true" == project.findProperty("skipPush")

    fun branch(): String = perform("branch", "--no-color")
            .filter { it.startsWith("*") }
            .findFirst()
            .map { it.replace(Regex("""\*|\s+"""), "") }
            .get()

    fun status(): Stream<String> = git("status")

    fun add(file: File): Stream<String> = git("add", file.path)

    fun commit(message: String): Stream<String> = git("commit", "-m", "[Bot] $message")

    fun tag(version: SemVer): Stream<String> = git("tag", "-a", version.toString(), "-m", "[Bot] Release $version: create tag")

    fun checkout(branch: String): Stream<String> = git("checkout", "-f", "-B", branch, "origin/$branch")

    fun merge(branch: String): Stream<String> = git("merge", branch, "--no-edit", "-m", "[Bot] merge $branch")

    fun push(): Stream<String> {
        if (skipPush) return Stream.of("Skip push!")

        return Stream.of(
                git("push", "--all"),
                git("push", "--tags")
        ).flatMap { it }
    }

    fun git(vararg arguments: String): Stream<String> {
        logger.info("GIT: On branch ${branch()}")
        logger.info("GIT: git {}", arguments)
        status().forEach{logger.info("GIT: $it")}
        val output = perform(*arguments)
        return output.asSequence().onEach { logger.info("GIT: $it") }.asStream()
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
                throw GradleException("exit $exitValue; \nCommand 'git $arguments' failed!\n${errors.joinToString("\n")}")

            return process.inputStream.bufferedReader().lines()
        } catch (e: IOException) {
            throw GradleException("Failed to execute command '$this'", e)
        }
    }


}
