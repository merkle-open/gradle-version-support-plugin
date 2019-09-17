package com.namics.oss.gradle.version

import org.gradle.api.Project

/*
 * Copyright 2000-2019 Namics AG. All rights reserved.
 */
public open class VersionSupportExtension(project: Project) {
    var majorBranches: List<Regex> = emptyList()
    var minorBranches: List<Regex> = listOf(Regex("""^develop.*"""))
    var patchBranches: List<Regex> = listOf(Regex("""^hotfix.*"""))
    var git: GitManager = NativeGitManager(project)
}
