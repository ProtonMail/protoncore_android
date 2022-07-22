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

import java.io.BufferedWriter
import java.io.File
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import format.GitlabQualityReport
import format.SarifQualityReport
import io.gitlab.arturbosch.detekt.Detekt
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.rubygrapefruit.platform.file.FilePermissionException
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import studio.forface.easygradle.dsl.`detekt version`
import studio.forface.easygradle.dsl.`detekt-cli`
import studio.forface.easygradle.dsl.`detekt-formatting`

@Suppress("unused")
abstract class ProtonDetektPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val configuration = target.extensions.create<ProtonDetektConfiguration>("protonDetekt")
        target.afterEvaluate { setupDetekt(configuration) }
    }
}

abstract class ProtonDetektConfiguration {

    abstract val configFileProperty: Property<File>
    var configFile: File
        get() = configFileProperty.get()
        set(value) = configFileProperty.set(value)

    var configFilePath: File
        @Deprecated("Use configFile instead", ReplaceWith("configFile"))
        get() = configFileProperty.get()
        @Deprecated("Use configFile instead", ReplaceWith("configFile = value"))
        set(value) = configFileProperty.set(value)

    abstract val customRulesConfigFileProperty: Property<File>
    var customRulesConfigFile: File
        get() = customRulesConfigFileProperty.get()
        set(value) = customRulesConfigFileProperty.set(value)

    abstract val reportDirProperty: Property<File>
    var reportDir: File
        get() = reportDirProperty.get()
        set(value) = reportDirProperty.set(value)

    abstract val mergedReportNameProperty: Property<String>
    var mergedReportName: String
        get() = mergedReportNameProperty.get()
        set(value) = mergedReportNameProperty.set(value)

    abstract val thresholdProperty: Property<Int>
    var threshold: Int?
        get() = thresholdProperty.orNull
        set(value) = thresholdProperty.set(value)
}

/**
 * Setup Detekt for whole Project.
 * It will:
 * * apply Detekt plugin to sub-projects
 * * configure Detekt Extension
 * * add accessor Detekt dependencies
 * * register [MergeDetektReports] Task, in order to generate an unique json report for all the
 *   module
 */
private fun Project.setupDetekt(configuration: ProtonDetektConfiguration) {

    `detekt version` = "1.19.0" // Released: May 15, 2021

    val defaultConfigFilePath = "config/detekt/config.yml"

    configuration.configFileProperty.convention(File(rootDir, defaultConfigFilePath))
    configuration.customRulesConfigFileProperty.convention(File(rootDir, "config/detekt/custom-rules.yml"))
    configuration.mergedReportNameProperty.convention("mergedReport.json")
    configuration.reportDirProperty.convention(File(rootDir, "config/detekt/reports"))
    configuration.thresholdProperty.convention(null)

    val configFile = configuration.configFile
    val customRulesConfigFile = configuration.customRulesConfigFile

    if (rootProject.name != "Proton Core") {
        downloadDetektConfig(githubConfigFilePath = defaultConfigFilePath, to = configFile)
    }

    applyCustomThresholdIfAvailable(thresholdProperty = configuration.thresholdProperty, configFile = configFile)

    if (!configFile.exists()) {
        println("Detekt configuration file not found!")
        return
    }

    subprojects.forEach { sub ->
        sub.repositories.mavenCentral()
        sub.apply(plugin = "io.gitlab.arturbosch.detekt")
    }

    if (!configuration.reportDir.exists()) {
        configuration.reportDir.mkdirs()
    }

    subprojects.forEach { sub ->

        sub.tasks.withType<Detekt> {
            autoCorrect = true
            if (customRulesConfigFile.exists()) config.from(customRulesConfigFile, configFile)
            else config.from(configFile)
            reports {
                xml.required.set(false)
                html.required.set(false)
                txt.required.set(false)
                sarif.required.set(true)
            }
        }

        sub.dependencies {
            add("detekt", `detekt-cli`)
            add("detektPlugins", `detekt-formatting`)
        }
    }

    val convertToGitlabFormat = tasks.register<ConvertToGitlabFormat>("convertToGitlabFormat") {
        reportsDir = configuration.reportDir

        // Execute after 'detekt' is completed for sub-projects
        dependsOn(getTasksByName("detekt", true))
    }

    tasks.register<MergeDetektReports>("multiModuleDetekt") {
        reportsDir = configuration.reportDir
        outputName = configuration.mergedReportName
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

    private fun Project.generateReport() {
        subprojects.forEach { project ->
            project.generateReport()
        }
        val report = File(reportsDir, "${project.name}.json")
            .apply { if (exists()) writeText("") }

        val detektReports = File(project.buildDir, "reports/detekt")
        println("Looking for detekt.sarif in $detektReports")
        detektReports
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
                        ),
                        severity = result.level
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
        println("Merging detekt reports starting...")
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
            println("Merging reports from files $reportFiles")
            val nonEmptyReports = reportFiles.filter { it.length() != 0L }

            writer.handleFile(nonEmptyReports.first())
            nonEmptyReports.drop(1).forEach {
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

@Suppress("TooGenericExceptionCaught", "SameParameterValue")
private fun downloadDetektConfig(githubConfigFilePath: String, to: File) {

    val dir = to.parentFile
    if (dir.exists().not() && dir.mkdirs().not()) {
        throw FilePermissionException("Cannot create directory ${dir.canonicalPath}")
    }

    if (to.isLessThanADayOld) {
        println("Detekt rule-set is less than a day old, skipping download.")
        return
    }

    val url = "https://raw.githubusercontent.com/ProtonMail/protoncore_android/master/$githubConfigFilePath"
    println("Fetching Detekt rule-set from $url")
    try {
        val content = URL(url).openStream().bufferedReader().readText()
        // Checking start of the file is enough, if some part is missing we would not be able to decode it
        require(content.startsWith("# Integrity check *")) { "Integrity check not passed" }

        to.bufferedWriter().use { writer ->
            writer.write("# ${DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())}\n")
            writer.write(content)
        }

    } catch (t: Throwable) {
        println("Cannot download Detekt configuration: ${t.message}")
    }
}

private fun applyCustomThresholdIfAvailable(thresholdProperty: Property<Int>, configFile: File) {
    if (thresholdProperty.isPresent) {
        val threshold = thresholdProperty.get()
        println("Applying custom threshold of $threshold")
        val newContent = configFile.readText()
            .replace(Regex(" {2}maxIssues: \\d+"), "  maxIssues: $threshold")
        configFile.writeText(newContent)

    } else {
        println("No custom threshold found, using default threshold.")
    }
}

private val File.isLessThanADayOld: Boolean
    get() = exists() && useLines { lines -> lines.firstOrNull() }?.let { line ->
        try {
            DateTimeFormatter.ISO_DATE_TIME.parse(line.substring(2), LocalDateTime::from)
        } catch (e: Exception) {
            null
        }
    }?.isAfter(LocalDateTime.now().minusDays(1)) == true
