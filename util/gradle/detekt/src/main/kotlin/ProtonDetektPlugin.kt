/*
 * Copyright (c) 2021 Proton Technologies AG
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

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.rubygrapefruit.platform.file.FilePermissionException
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import format.GitlabQualityReport
import format.SarifQualityReport
import studio.forface.easygradle.dsl.*
import java.io.BufferedWriter
import java.io.File
import java.net.URL

abstract class ProtonDetektPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.setupDetekt()
    }
}

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
private fun Project.setupDetekt(filter: (Project) -> Boolean = { true }) {

    `detekt version` = "1.18.0" // Released: Aug 12, 2021

    val reportsDirPath = "config/detekt/reports"
    val configFilePath = "config/detekt/config.yml"

    val detektReportsDir = File(rootDir, reportsDirPath)
    val configFile = File(rootDir, configFilePath)

    if (rootProject.name != "Proton Core") {
        downloadDetektConfig(configFilePath, configFile)
    }

    if (!configFile.exists()) {
        println("Detekt configuration file not found!")
        return
    }

    // Configure sub-projects
    for (sub in subprojects.filter(filter)) {

        sub.apply(plugin = "io.gitlab.arturbosch.detekt")
        sub.extensions.configure<DetektExtension> {
            buildUponDefaultConfig = true
            allRules = false
            config = files(configFile)
            source = files(sub.projectDir.path + "/src/")

            reports {
                xml.enabled = false
                html.enabled = false
                txt.enabled = false
                sarif.enabled = true
            }
        }
        sub.dependencies {
            add("detekt", `detekt-cli`)
            add("detektPlugins", `detekt-formatting`)
        }
    }

    val convertToGitlabFormat = tasks.register<ConvertToGitlabFormat>("convertToGitlabFormat") {
        reportsDir = detektReportsDir

        // Execute after 'detekt' is completed for sub-projects
        val subTasks = subprojects.flatMap { getTasksByName("detekt", true) }
        dependsOn(subTasks)
    }

    tasks.register<MergeDetektReports>("multiModuleDetekt") {
        reportsDir = detektReportsDir
        dependsOn(convertToGitlabFormat)
    }
}

internal open class ConvertToGitlabFormat : DefaultTask() {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @InputDirectory
    lateinit var reportsDir: File

    @TaskAction
    fun run() = project.generateReport()

    @OptIn(ExperimentalSerializationApi::class)
    private fun Project.generateReport() {
        subprojects.forEach { project ->
            project.generateReport()
        }
        val report = File(reportsDir, "${project.name}.json")
            .apply { if (exists()) writeText("") }

        println("Looking for detekt.sarif in $reportsDir")
        File(project.buildDir, "reports/detekt")
            .listFiles { _, name -> name == "detekt.sarif" }
            ?.firstOrNull()
            ?.let { file ->
                json.decodeFromString<SarifQualityReport>(file.readText())
            }?.runs?.flatMap { run ->
                run.results
            }?.flatMap { result ->
                result.locations.map { location ->
                    GitlabQualityReport(
                        description = result.message.text,
                        fingerprint = "${location.physicalLocation.artifactLocation.uri}:${location.physicalLocation.region.startLine}",
                        location = GitlabQualityReport.Location(
                            lines = GitlabQualityReport.Location.Lines(
                                begin = location.physicalLocation.region.startLine,
                                end = location.physicalLocation.region.startLine,
                            ),
                            path = location.physicalLocation.artifactLocation.uri
                        )
                    )
                }
            }?.takeIf { results ->
                results.isNotEmpty()
            }?.let { results ->
                report.writeText(json.encodeToString(results))
                println("Report in ${report.absolutePath}")
            }
    }
}

internal open class MergeDetektReports : DefaultTask() {
    @InputDirectory
    lateinit var reportsDir: File

    @Input
    var outputName: String = "mergedReport.json"

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

    val dir = to.parentFile
    if (dir.exists().not() && dir.mkdirs().not())
        throw FilePermissionException("Cannot create directory ${dir.canonicalPath}")

    val url = "https://raw.githubusercontent.com/ProtonMail/protoncore_android/master/$path"
    println("Fetching Detekt rule-set from $url")
    try {
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

