/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

import ProtonIncludeCoreBuildExtension.Companion.createExtension
import me.champeau.gradle.igp.GitIncludeExtension
import me.champeau.gradle.igp.IncludeGitPlugin
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import java.io.File

abstract class ProtonIncludeCoreBuildPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        // Configuration from Client.
        val config = target.createExtension() as DefaultProtonIncludeCoreBuildExtension
        // Configuration from GitLab CI.
        val host = System.getenv("CI_SERVER_HOST")
        val token = System.getenv("CI_JOB_TOKEN")
        val hasHostToken = host != null && token != null
        val commitSha = System.getenv("CORE_COMMIT_SHA")
        val parentPath = target.rootDir.parentFile.absolutePath
        val metaProperties = File("$parentPath/${metaProperties}")
        target.gradle.settingsEvaluated {
            val protonLibsUri = when {
                config.uri.isPresent -> config.uri.get()
                hasHostToken -> "https://gitlab-ci-token:$token@$host/proton/mobile/android/proton-libs.git"
                else -> "https://github.com/ProtonMail/protoncore_android.git"
            }
            // Configuration for Core.
            target.apply<IncludeGitPlugin>()
            target.configure<GitIncludeExtension> {
                refreshIntervalMillis.set(config.refreshIntervalMillis.orNull)
                checkoutsDirectory.set(target.rootDir.parentFile)
                when {
                    metaProperties.exists() -> includeBuild(parentRepoDir)
                    commitSha != null -> include(coreRepoDir) {
                        uri.set(protonLibsUri)
                        commit.set(commitSha)
                    }
                    config.hasIncludedBuilds() -> include(coreRepoDir) {
                        uri.set(protonLibsUri)
                        config.configureCoreBuild(this)
                    }
                }
                // Configuration of additional IncludedGitRepo.
                config.configureIncludedGitRepos(this)
            }
        }
    }

    companion object {
        const val coreRepoDir = "proton-libs"
        const val parentRepoDir = "../$coreRepoDir"
        const val metaProperties = "meta.properties"
    }
}
