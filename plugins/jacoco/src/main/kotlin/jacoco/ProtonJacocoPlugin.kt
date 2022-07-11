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

import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.Node
import jacoco.extensions.ProtonCoverageMultiModuleExtension
import jacoco.extensions.ProtonCoverageMultiModuleExtension.Companion.setupCoverageMultiModuleExtension
import jacoco.extensions.ProtonCoverageTaskExtension.Companion.setupCoverageExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.utils.toSetOrEmpty

class ProtonJacocoPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val multiModuleExtension = setupCoverageMultiModuleExtension()
            subprojects { configureJacocoTestTask(multiModuleExtension) }
            configureCoberturaConversion(multiModuleExtension)
        }
    }

    private fun Project.configureCoberturaConversion(multiModuleExtension: ProtonCoverageMultiModuleExtension) {
        val defaultReportsDir = file("$buildDir/reports/jacoco/jacocoTestReport")
        val defaultReportFile = File(defaultReportsDir, "jacocoTestReport.xml")
        val defaultCoberturaOutputFile = File(buildDir, "reports").resolve(CoberturaFileName)
        val defaultCoverageConversionScript = file("$rootDir/plugins/jacoco/scripts/cover2cover.py")

        afterEvaluate {
            val generatesMergedXmlReports = multiModuleExtension.generatesMergedXmlReport(this)
            val xmlReportFile = multiModuleExtension.getXmlReportFile(this, defaultReportFile)

            val generatesMergedHtmlReports = multiModuleExtension.generatesMergedHtmlReport(this)
            val htmlReportFile = multiModuleExtension.getHtmlReportFile(this, defaultReportFile.parentFile)

            val coberturaFile = multiModuleExtension.getCoberturaFile(project, defaultCoberturaOutputFile)
            val coverageConversationScriptPath = multiModuleExtension.getCoverageConversionScript(
                project,
                defaultCoverageConversionScript
            ).absolutePath

            applyJacocoPlugin()
            registerMergeReportTask(generatesMergedXmlReports, xmlReportFile, generatesMergedHtmlReports, htmlReportFile)
            registerLogCoverageReportTask(xmlReportFile)
            registerCoberturaConversionTask(xmlReportFile, coberturaFile, coverageConversationScriptPath)
        }
    }

    private fun Project.applyJacocoPlugin() {
        repositories.mavenCentral()
        plugins.apply(JacocoPlugin::class)
        configure<JacocoPluginExtension> {
            toolVersion = JacocoVersion
        }
    }

    /**
     * Generates an aggregated Jacoco report for all modules
     */
    private fun Project.registerMergeReportTask(
        generateXmlReport: Boolean,
        xmlReportFile: File,
        generateHtmlReport: Boolean,
        htmlReportFile: File,
    ) {
        tasks.register<JacocoReport>(MergeReportTaskName) {
            group = "Verification"
            description = "Generates aggregated Jacoco reports for all modules"

            val jacocoSubTasks = getReportTasks(this)
            dependsOn(jacocoSubTasks)

            val source = jacocoSubTasks.map { it.sourceDirectories }
            additionalSourceDirs.setFrom(source)
            sourceDirectories.setFrom(source)

            classDirectories.setFrom(jacocoSubTasks.map { it.classDirectories })
            executionData.setFrom(jacocoSubTasks.map { it.executionData.asFileTree })

            reports {
                xml.required.set(generateXmlReport)
                xml.outputLocation.set(xmlReportFile)

                html.required.set(generateHtmlReport)
                html.outputLocation.set(htmlReportFile)
            }

            doLast {
                if (generateXmlReport) println("Writing aggregated XML report to: $xmlReportFile")
                if (generateHtmlReport) println("Writing aggregated HTML report to: $htmlReportFile")
            }
        }
    }

    /**
     * Used to log the total coverage % so the CI can read the logs and record that as the branch's coverage
     */
    private fun Project.registerLogCoverageReportTask(xmlReportFile: File) {
        tasks.register(CoverageLogReportTaskName) {
            group = "Verification"
            description = "Logs the overall percentage of the project"

            dependsOn(MergeReportTaskName)

            doLast {
                val slurper = XmlSlurper()
                slurper.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
                slurper.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false
                )
                val xml = slurper.parse(xmlReportFile)
                val counter = xml.childNodes().asSequence().firstOrNull {
                    (it as? Node)?.name() == "counter" && it.attributes()["type"] == "INSTRUCTION"
                } as? Node ?: return@doLast
                val missed = (counter.attributes()["missed"] as String).toInt()
                val covered = (counter.attributes()["covered"] as String).toInt()
                val total = (missed + covered).toFloat()
                val percentage = covered / total * 100.0f

                // This will print the total percentage into the build logs. Gitlab will then parse that line
                // and show the coverage percentage in the MR info and repo statistics.
                // See: https://docs.gitlab.com/ee/ci/pipelines/settings.html#add-test-coverage-results-to-a-merge-request
                println("Covered %d/%d lines".format(covered, missed))
                println("Total %.2f%%".format(Locale.US, percentage))
            }
        }
    }

    /**
     * Used to convert from Jacoco XML report files to Cobertura ones, compatible with Gitlab
     */
    private fun Project.registerCoberturaConversionTask(
        xmlReportFile: File,
        coberturaOutputFile: File,
        coverageConversationScriptPath: String
    ) {
        tasks.register<Exec>(CoberturaReportConversionTaskName) {
            group = "Verification"
            description = "Converts Jacoco XML report to Cobertura"

            dependsOn(CoverageLogReportTaskName)

            coberturaOutputFile.parentFile.mkdirs()
            workingDir = rootDir

            val mergeReportTask = tasks.withType<JacocoReport>().firstOrNull { it.name == MergeReportTaskName }
            val sources = mergeReportTask?.sourceDirectories.toSetOrEmpty()

            doFirst {
                standardOutput = FileOutputStream(coberturaOutputFile)
            }

            // Convert Jacoco merged coverage report into a Cobertura one, which is supported by Gitlab.
            // See: https://docs.gitlab.com/ee/user/project/merge_requests/test_coverage_visualization.html#java-and-kotlin-examples
            commandLine(
                "python3",
                coverageConversationScriptPath,
                xmlReportFile.absolutePath,
                *sources.toTypedArray(),
            )
        }
    }

    /**
     * Configures the jacocoTestReport tasks for each submodule
     */
    private fun Project.configureJacocoTestTask(
        multiModuleExtension: ProtonCoverageMultiModuleExtension,
        srcFolder: String = "kotlin"
    ) {
        val taskExtension = setupCoverageExtension()
        val mainSrc = "src/main/$srcFolder"
        val sourceDirs = (taskExtension.sourceDirs + mainSrc).toSet()

        val hasSourceDirs = sourceDirs.any { file(it).exists() }

        // Don't setup Jacoco if there are no sources or Jacoco was manually disabled
        if (!taskExtension.isEnabled || !hasSourceDirs) return

        applyJacocoPlugin()

        val jacocoConfig: JacocoReport.() -> Unit = {
            reports {
                val generateXmlReports = taskExtension.generatesXmlReport?.invoke(project)
                    ?: multiModuleExtension.generatesSubModuleXmlReports(project)
                val generateHtmlReports = taskExtension.generatesHtmlReport?.invoke(project)
                    ?: multiModuleExtension.generatesSubModuleHtmlReports(project)
                xml.required.set(generateXmlReports)
                html.required.set(generateHtmlReports)
            }

            val fileFilter = multiModuleExtension.sharedExcludes + taskExtension.excludes + DefaultExcludedFiles
            val androidDebugTree = fileTree("$buildDir/tmp/kotlin-classes/debug") { exclude(fileFilter) }
            val jvmDebugTree = fileTree("$buildDir/classes/kotlin/main") { exclude(fileFilter) }

            sourceDirectories.setFrom(sourceDirs)
            classDirectories.setFrom(androidDebugTree, jvmDebugTree)
            executionData.setFrom(fileTree(buildDir) { include(listOf("**/*.exec", "**/*.ec")) })

            if (multiModuleExtension.runTestTasksBefore) {
                if (taskExtension.dependsOnTask != null) {
                    dependsOn(taskExtension.dependsOnTask)
                } else {
                    // Depends on debug unit test tasks
                    dependsOn(
                        tasks.withType<Test>().filter {
                            it.name == "test" || it.name.endsWith("DebugUnitTest")
                        }
                    )
                }
            }
        }

        // Workaround for Jacoco issues when run from the IDE
        tasks.withType<Test> {
            configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }

        afterEvaluate {
            // Don't setup Jacoco if there are no sources or Jacoco was manually disabled
            if (!taskExtension.isEnabled || !hasSourceDirs) return@afterEvaluate

            val isJacocoRegistered = tasks.findByName(JacocoReportTaskName) != null

            if (!isJacocoRegistered) {
                println("Registering Jacoco for $name")
                tasks.register(JacocoReportTaskName, jacocoConfig)
            } else {
                println("Configuring registered Jacoco for $name")
                tasks.getByName(JacocoReportTaskName, jacocoConfig)
            }
        }
    }

    private fun Project.getReportTasks(parentTask: JacocoReport): List<JacocoReport> {
        return allprojects.flatMap { project ->
            project.tasks.withType<JacocoReport>().filter { it.name == JacocoReportTaskName && it != parentTask }
        }
    }

    private fun ProtonCoverageMultiModuleExtension.getXmlReportFile(project: Project, default: File) =
        mergedXmlReportPath?.invoke(project)?.let { project.file(it) } ?: default

    private fun ProtonCoverageMultiModuleExtension.getHtmlReportFile(project: Project, default: File) =
        mergedHtmlReportPath?.invoke(project)?.let { project.file(it) } ?: default

    private fun ProtonCoverageMultiModuleExtension.getCoberturaFile(project: Project, default: File) =
        coberturaReportPath?.invoke(project)?.let { project.file(it) } ?: default

    private fun ProtonCoverageMultiModuleExtension.getCoverageConversionScript(project: Project, default: File) =
        coverageConversionScript?.invoke(project)?.let { project.file(it) } ?: default

    companion object {
        const val JacocoVersion = "0.8.8"
        const val MergeReportTaskName = "jacocoMergeReport"
        const val CoverageLogReportTaskName = "coverageReport"
        const val CoberturaReportConversionTaskName = "coberturaCoverageReport"
        const val JacocoReportTaskName = "jacocoTestReport"

        const val CoberturaFileName = "cobertura-coverage.xml"

        val DefaultExcludedFiles = listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            // DI code, doesn't need testing
            "**/*Module.class",
            "**/*Module$*",
        )
    }
}
