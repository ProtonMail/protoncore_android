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

@file:Suppress("unused")

package me.proton.core.util.gradle

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import studio.forface.easygradle.dsl.*
import java.io.BufferedWriter
import java.io.File
import java.net.URL

/**
 * Setup Detekt for whole Project.
 * It will:
 * * apply Detekt plugin to sub-projects
 * * configure Detekt Extension
 * * add accessor Detekt dependencies
 * * register [MergeDetektReports] Task, in order to generate an unique json report for all the
 *   module
 *
 * @param filter filter [Project.subprojects] to attach Detekt to
 *
 *
 * @author Davide Farella
 */
fun Project.setupDetekt(filter: (Project) -> Boolean = { true }) {

    `detekt version` = "1.11.2" // Released: Aug 19, 2020
    `detect-code-analysis version` = "0.3.2" // Released:

    val reportsDirPath = "config/detekt/reports"
    val configFilePath = "config/detekt/config.yml"

    val detektReportsDir = File(rootDir, reportsDirPath)
    val configFile = File(rootDir, configFilePath)

    if (rootProject.name != "Proton Core")
        downloadDetektConfig(configFilePath, configFile)

    if (!configFile.exists()) {
        println("Detekt configuration file not found!")
        return
    }

    // Configure sub-projects
    for (sub in subprojects.filter(filter)) {

        sub.apply(plugin = "io.gitlab.arturbosch.detekt")
        sub.extensions.configure<DetektExtension> {

            failFast = false
            config = files(configFile)
            input = files(sub.projectDir.path + "/src/")

            reports {
                xml.enabled = false
                html.enabled = false
                txt.enabled = false
                custom {
                    reportId = "DetektQualityOutputReport"
                    destination = File(detektReportsDir, "${sub.name}.json")
                }
            }
        }
        sub.dependencies {
            add("detekt", `detekt-cli`)
            add("detektPlugins", `detekt-code-analysis`)
            add("detektPlugins", `detekt-formatting`)
        }

    }

    tasks.register<MergeDetektReports>("multiModuleDetekt") {
        reportsDir = detektReportsDir

        // Execute after 'detekt' is completed for sub-projects
        val subTasks = subprojects.flatMap { getTasksByName("detekt", true) }
        dependsOn(subTasks)
    }

}

internal open class MergeDetektReports : DefaultTask() {
    @InputDirectory lateinit var reportsDir: File
    @Input var outputName: String = "mergedReport.json"

    @TaskAction
    fun run() {
        val mergedReport = File(reportsDir, outputName)
            .apply { if (exists()) writeText("") }

        mergedReport.bufferedWriter().use { writer ->
            val reportFiles = reportsDir
                // Take json files, excluding the merged report
                .listFiles { _, name -> name.endsWith(".json") && name != outputName }
                ?.filterNotNull()
                // Skip modules without issues
                ?.filter {
                    it.bufferedReader().use { reader ->
                        return@filter reader.readLine() != "[]"
                    }
                }
                // Return if no file is found
                ?.takeIf { it.isNotEmpty() } ?: return


            // Open array
            writer.append("[")

            // Write body
            writer.handleFile(reportFiles.first())
            reportFiles.drop(1).forEach {
                writer.append(",")
                writer.handleFile(it)
            }

            // Close array
            writer.newLine()
            writer.append("]")
        }
    }

    private fun BufferedWriter.handleFile(file: File) {
        val allLines = file.bufferedReader().lineSequence()

        // Drop first and write 'prev' in order to skip array open and close
        var prev: String? = null
        allLines.drop(1).forEach { s ->
            prev?.let {
                newLine()
                append(it)
            }
            prev = s
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Suppress("TooGenericExceptionCaught", "SameParameterValue")
private fun downloadDetektConfig(path: String, to: File) {
    try {
        val url = "https://raw.githubusercontent.com/ProtonMail/protoncore_android/master/$path"
        println("Fetching Detekt rule-set from $url")
        val content = URL(url).openStream().bufferedReader().readText()
        // Checking start of the file is enough, if some part is missing we would not be able to decode it
        require(content.startsWith("# Integrity check *")) { "Integrity check not passed" }

        to.bufferedWriter().use { writer ->
            writer.write(content)
        }

    } catch (t: Throwable) {
        println("Cannot download Detekt configuration: ${t.message}")
    }
}
