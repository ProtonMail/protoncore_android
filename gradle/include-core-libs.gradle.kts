/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.io.ByteArrayOutputStream
import java.io.File

/**
 * If `useCoreGitSubmodule` property is set to `"true"`, this script adds `proton-libs` submodule as an included build.
 * It also makes sure that we are building against the desired commit of the proton-libs.
 * If `CORE_COMMIT_SHA` env variable exists, the desired commit is read from it.
 * Otherwise, the desired commit is defined by the parent repository.
 * If the desired commit is different than the current one, a warning is printed.
 */

if (System.getenv("ORG_GRADLE_PROJECT_useCoreGitSubmodule") != null) {
    println("Property `useCoreGitSubmodule` overriden by `ORG_GRADLE_PROJECT_useCoreGitSubmodule` env variable.")
}

if (extensions.extraProperties.properties["useCoreGitSubmodule"].toString().toBoolean()) {
    includeProtonLibsBuild()
} else {
    println("Core libs from Maven artifacts")
}

fun includeProtonLibsBuild() {
    val coreSubmoduleDir = extensions.extraProperties.properties["coreSubmoduleDir"] as File?
    val protonLibsDir: File = coreSubmoduleDir ?: File(rootProject.projectDir, "proton-libs")

    if (!protonLibsDir.exists()) {
        error("Submodule `$protonLibsDir` does not exist.")
    } else if (!protonLibsDir.isDirectory()) {
        error("Submodule `$protonLibsDir` is not a directory.")
    } else if (protonLibsDir.listFiles()?.any { it.name == "settings.gradle.kts" } != true) {
        error("Submodule `$protonLibsDir` is not checked out.")
    } else {
        // Make sure we're including the correct commit from proton-libs:
        val revParseOutput = ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-parse", "HEAD")
            workingDir = protonLibsDir
            standardOutput = revParseOutput
        }

        val currentHash = revParseOutput.toString().trim()
        val requiredHash = System.getenv("CORE_COMMIT_SHA") ?: run {
            val submoduleStatusOutput = ByteArrayOutputStream()
            exec {
                commandLine("git", "submodule", "status", "--cached", "--", "proton-libs")
                standardOutput = submoduleStatusOutput
            }
            // Output is in form: ` 257889f6c1fbb4bd742bfdeb6fc8d4d8d8a10588 proton-libs (release/libs/3.0.0-319-g257889f6)`
            // and the first character can be a space, `-`, or `+`.
            submoduleStatusOutput.toString().substring(1).trim().split(" ").first()
        }

        if (currentHash != requiredHash) {
            logger.warn("The ${protonLibsDir.name} submodule is expected to be at $requiredHash, but is at $currentHash.")
        }

        includeBuild("proton-libs")
        println("Core libs from git submodule `$protonLibsDir` at $currentHash")
    }
}
