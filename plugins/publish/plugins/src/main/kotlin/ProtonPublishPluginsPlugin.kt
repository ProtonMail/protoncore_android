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

import com.gradle.publish.PluginBundleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.named
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import studio.forface.easygradle.dsl.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

abstract class ProtonPublishPluginsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.subprojects { this@ProtonPublishPluginsPlugin.apply(this) }
        target.setupModule()
    }
}


/**
 * Setup Publishing for whole Project.
 * It will setup publishing to sub-projects by generating KDoc, generating aar, updating readme and publish new versions
 * to Bintray
 *
 * @param filter filter [Project.subprojects] to attach Publishing to
 *
 *
 * @author Davide Farella
 */
fun Project.setupPublishing(filter: (Project) -> Boolean = { true }) {

    // Configure sub-projects
    for (sub in subprojects.filter(filter)) sub.setupModule()
}

private fun Project.setupModule() {
    afterEvaluate {

        val key = extra.properties["gradle.publish.key"] ?: System.getenv()["GRADLE_PORTAL_KEY"]
        val secret = extra.properties["gradle.publish.secret"] ?: System.getenv()["GRADLE_PORTAL_SECRET"]
        extra["gradle.publish.key"] = key
        extra["gradle.publish.secret"] = secret

        if (pluginConfig != null) {
            apply<DokkaPlugin>()

            archivesBaseName = archiveName

            // Setup Gradle publish config
            apply(plugin = "com.gradle.plugin-publish")
            configure<PluginBundleExtension> {
                val url = "https://github.com/ProtonMail/protoncore_android"
                website = url
                vcsUrl = url
                description = "Proton Gradle plugin"
                tags = listOf(
                    "Android",
                    "plugin",
                    "Proton",
                    "ProtonTechnologies",
                    "ProtonMail",
                    "ProtonVpn",
                    "ProtonCalendar",
                    "ProtonDrive"
                )

                plugins.getByName(pluginConfig!!.id).displayName = pluginConfig!!.name
            }

            with(ReleaseManager(this)) {
                if (key != null && secret != null) {

                    // Setup pre publish
                    val prePublish = tasks.create("prePublish") {
                        doFirst {
                            generateKdocIfNeeded()
                            updateReadme()
                            printToNewReleasesFile()
                        }
                    }

                    // Setup publish
                    tasks.register("publishAll") {
                        dependsOn(prePublish)
                        if (isNewVersion)
                            dependsOn(tasks.getByName("publishPlugins"))
                    }
                } else {
                    // Force Dokka update BEING AWARE THAT IS NOT SUPPOSED TO BE COMMITTED
                    tasks.create("forceDokka") {
                        doLast { generateKdocIfNeeded() }
                    }
                    // Force Readme update BEING AWARE THAT IS NOT SUPPOSED TO BE COMMITTED
                    tasks.create("forceUpdateReadme") {
                        doLast { updateReadme() }
                    }
                }
            }
        }
    }
}

/**
 * This class will organize plugins release.
 *
 * It can:
 * * Generate KDoc if new plugin is available
 * * Update readme with new version
 * * Print update to new_releases.tmp
 *
 * @param forceRefresh Generally all the processes are executed only of [Project.pluginConfig] [PluginConfig.version]
 * is different from the one in the readme. If this parameter is set to `true`, they will run in any case.
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
    private val versionName = pluginConfig!!.version.versionName

    val isNewVersion = forceRefresh || prevVersion != versionName
    private val shouldRefresh = forceRefresh || isNewVersion

    /** Generate KDoc if new library is available */
    fun generateKdocIfNeeded() {
        if (shouldRefresh) {
            val task = tasks.named<DokkaTask>("dokkaHtml").orNull ?: return
            task.taskActions.forEach { it.execute(task) }
        }
    }

    /** Update readme with new version */
    fun updateReadme() {
        if (shouldRefresh) {
            val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

            README_FILE.writeText(
                README_FILE.readText()
                    .replace(README_VERSION_REGEX, readmeVersion(humanReadableName, versionName, timestamp))
            )
        }
    }

    /** Print release to new_releases.tmp */
    fun printToNewReleasesFile() {
        if (shouldRefresh) {
            NEW_RELEASES_FILE.writeText(
                "${NEW_RELEASES_FILE.readText()}$humanReadableName $versionName\n"
            )
        }
    }

    private companion object {
        val Project.README_FILE get() = File(rootDir.parentFile, "README.md")
        val Project.README_VERSION_REGEX get() =
            readmeVersion("^$humanReadableName", "(.+)", "(.+)").toRegex(RegexOption.MULTILINE)
        val Project.NEW_RELEASES_FILE get() = File(rootDir.parentFile.parentFile, "new_releases.tmp")
            .also { file ->
                // 10 min lifetime
                if (file.lastModified() < System.currentTimeMillis() - 10 * 60 * 1000) file.delete()
                if (!file.exists()) file.createNewFile()
            }

        fun readmeVersion(name: String, version: String, timestamp: String) =
            """$name: \*\*$version\*\* - _released on: ${timestamp}_"""
    }
}
