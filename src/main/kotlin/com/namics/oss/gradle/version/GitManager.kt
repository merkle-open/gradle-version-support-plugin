package com.namics.oss.gradle.version

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.lang.ProcessBuilder.Redirect.PIPE
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

class GitManager(private val project: Project) {

    val root = project.projectDir;
    val timoutSeconds = 120L
    val logger = project.logger

    fun branch(): String = git("branch", "--no-color")
            .filter { it.startsWith("*") }
            .findFirst()
            .map { it.replace(Regex("""\*|\s+"""), "") }
            .get()

    fun status(): Stream<String> = git("branch", "status")

    fun add(file: File): Stream<String> = git("add", file.path)

    fun commit(message: String): Stream<String> = git("commit", "-m", "[Bot] $message")

    fun tag(version: SemVer): Stream<String> = git("tag", "-a", version.toString(), "-m", "[Bot] Release $version: create tag")

    fun checkout(branch: String): Stream<String> = git("checkout", "-f", "-B", branch, "origin/$branch")

    fun merge(branch: String): Stream<String> = git("merge", branch, "--no-edit", "-m", "[Bot] merge $branch")

    fun push(): Stream<String> {
        return Stream.of(
                git("push", "--all"),
                git("push", "--tags")
        ).flatMap { it }
    }

    fun git(vararg arguments: String): Stream<String> {
        logger.info("GIT: git {}", arguments)
        try {
            val process = ProcessBuilder("git", *arguments)
                    .directory(root)
                    .redirectOutput(PIPE)
                    .redirectError(PIPE)
                    .start()

            process.waitFor(timoutSeconds, TimeUnit.SECONDS)
            val errors = ArrayList<String>()
            process.errorStream.bufferedReader().lines().forEach {
                logger.error("GIT err: {}", it)
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
