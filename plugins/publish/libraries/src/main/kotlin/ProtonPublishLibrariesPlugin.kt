/*
 * Copyright (c) 2020 Proton Technologies AG
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

import PublishOptionExtension.Companion.setupPublishOptionExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import com.vanniktech.maven.publish.MavenPublishPluginExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaPlugin
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Setup Publishing for whole Project.
 *
 * Setup sub-projects by generating KDoc, generating aar, updating readme, sign and publish new versions to Maven.
 */
abstract class ProtonPublishLibrariesPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.setupPublishing()

        target.subprojects {
            apply<ProtonPublishLibrariesPlugin>()
        }
    }
}

private fun Project.setupPublishing() {
    val publishOption = setupPublishOptionExtension()
    afterEvaluate {
        if (publishOption.shouldBePublishedAsLib) {
            setupCoordinates()
            setupReleaseTask()
        }
    }
}

private fun Project.setupCoordinates() {
    group = "me.proton.core"
    val artifactId = name
    val versionName = "2.0.0-alpha01-SNAPSHOT"
    version = versionName
    apply<DokkaPlugin>()
    apply<MavenPublishPlugin>()
    configure<MavenPublishPluginExtension> {
        sonatypeHost = SonatypeHost.S01
        // Only sign non snapshot release
        releaseSigningEnabled = !versionName.contains("SNAPSHOT")
    }
    configure<MavenPublishBaseExtension> {
        pom {
            name.set(artifactId)
            description.set("Proton Core libraries for Android")
            url.set("https://github.com/ProtonMail/protoncore_android")
            licenses {
                license {
                    name.set("GNU GENERAL PUBLIC LICENSE, Version 3.0")
                    url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                }
            }
            developers {
                developer {
                    name.set("Open Source Proton")
                    email.set("opensource@proton.me")
                    id.set(email)
                }
            }
            scm {
                url.set("https://gitlab.protontech.ch/proton/mobile/android/proton-libs")
                connection.set("git@gitlab.protontech.ch:proton/mobile/android/proton-libs.git")
                developerConnection.set("https://gitlab.protontech.ch/proton/mobile/android/proton-libs.git")
            }
        }
    }
}

private fun Project.setupReleaseTask() {
    val releaseManager = ReleaseManager(this)
    tasks.register("publishLibrary") {
        if (releaseManager.isNewVersion) {
            dependsOn(tasks.named("publish"))
            doLast {
                releaseManager.updateReadme()
                releaseManager.printToNewReleasesFile()
            }
        }
    }
}

/**
 * This class will organize libraries release.
 *
 * It can:
 * * Update readme with new version
 * * Print update to new_releases.tmp
 *
 * @param forceRefresh Generally all the processes are executed only of [Project.libVersion] is different from the one
 *   in the readme. If this parameter is set to `true`, they will run in any case.
 *   Default is `false`
 *
 * @author Davide Farella
 */
class ReleaseManager internal constructor(
    project: Project,
    forceRefresh: Boolean = false
) : Project by project {

    private val prevVersion = README_VERSION_REGEX.find(README_FILE.readText())?.groupValues?.get(1)
        ?: throw IllegalArgumentException("Cannot find version for $name: $README_VERSION_REGEX")

    val isNewVersion = forceRefresh || prevVersion != version
    private val shouldRefresh = forceRefresh || isNewVersion

    /** Update readme with new version */
    fun updateReadme() {
        if (shouldRefresh) {
            val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

            README_FILE.writeText(
                README_FILE.readText()
                    .replace(README_VERSION_REGEX, readmeVersion(humanReadableName, version as String, timestamp))
            )
        }
    }

    /** Print release to new_releases.tmp */
    fun printToNewReleasesFile() {
        if (shouldRefresh) {
            NEW_RELEASES_FILE.writeText(
                "${NEW_RELEASES_FILE.readText()}$humanReadableName $version\n"
            )
        }
    }

    private companion object {
        val Project.README_FILE get() = File(rootDir, "README.md")
        val Project.README_VERSION_REGEX get() =
            readmeVersion("^$humanReadableName", "(.+)", "(.+)").toRegex(RegexOption.MULTILINE)
        val Project.NEW_RELEASES_FILE get() = File(rootDir, "new_releases.tmp")
            .also { file ->
                // 10 min lifetime
                if (file.lastModified() < System.currentTimeMillis() - 10 * 60 * 1000) file.delete()
                if (!file.exists()) file.createNewFile()
            }

        fun readmeVersion(name: String, version: String, timestamp: String) =
            """$name: \*\*$version\*\* - _released on: ${timestamp}_"""
    }
}

