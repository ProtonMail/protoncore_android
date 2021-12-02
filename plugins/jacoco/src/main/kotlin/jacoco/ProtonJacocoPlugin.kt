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

import groovy.xml.slurpersupport.Node
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class ProtonJacocoPlugin : Plugin<Project> {

    companion object {
        const val JacocoVersion = "0.8.7"
    }

    private fun Project.getReportTasks(jacocoReport: JacocoReport): List<JacocoReport> {
        return allprojects.flatMap {
            it.tasks.withType<JacocoReport>().filter { it.name == "jacocoTestReport" }
                .filter { report -> report != jacocoReport }
        }
    }

    override fun apply(target: Project) {
        with(target) {
            subprojects {
                afterEvaluate {
                    configureJacocoTestTask()
                }
            }

            configureCoberturaConversion()
        }
    }

    private fun Project.configureCoberturaConversion() {
        plugins.apply(JacocoPlugin::class)
        configure<JacocoPluginExtension> {
            toolVersion = JacocoVersion
        }

        val defaultReportsDir = file("$buildDir/reports/jacoco/jacocoTestReport")
        val reportFile = File(defaultReportsDir, "jacocoTestReport.xml")

        tasks.register<JacocoReport>("jacocoMergeReport") {
            group = "Verification"
            description = "Generate Jacoco aggregate report for all modules"

            val jacocoSubTasks = getReportTasks(this)
            dependsOn(jacocoSubTasks)

            val sourceDirs = jacocoSubTasks.flatMap { it.sourceDirectories }
            val source = files(sourceDirs)
            additionalSourceDirs.setFrom(source)
            sourceDirectories.setFrom(source)

            val classDirs = jacocoSubTasks.flatMap { it.classDirectories }
            classDirectories.setFrom(files(classDirs))

            // If exec files are not included here the task will be skipped
            executionData.setFrom(fileTree(rootDir) { include(listOf("**/*.exec", "**/*.ec")) })

            reports {
                html.required.set(true)
                html.outputLocation.set(defaultReportsDir)
                xml.required.set(true)
                xml.outputLocation.set(reportFile)
            }

            doLast { println("Writing aggregated report to: $reportFile") }
        }

        tasks.register("coverageReport") {
            dependsOn("jacocoMergeReport")

            doLast {
                val slurper = groovy.xml.XmlSlurper()
                slurper.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
                slurper.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false
                )
                val xml = slurper.parse(reportFile)
                val counter = xml.childNodes().asSequence().firstOrNull {
                    (it as? Node)?.name() == "counter" && it.attributes()["type"] == "INSTRUCTION"
                } as? Node ?: return@doLast
                val missed = (counter.attributes()["missed"] as String).toInt()
                val covered = (counter.attributes()["covered"] as String).toInt()
                val total = (missed + covered).toFloat()
                val percentage = (covered / total * 100.0f)

                // This will print the total percentage into the build logs. Gitlab will then parse that line
                // and show the coverage percentage in the MR info and repo statistics.
                // See: https://docs.gitlab.com/ee/ci/pipelines/settings.html#add-test-coverage-results-to-a-merge-request
                println("Missed %d branches".format(missed))
                println("Covered %d branches".format(covered))
                println("Total %.2f%%".format(Locale.US, percentage))
            }
        }

        tasks.register<Exec>("coberturaCoverageReport") {
            dependsOn("coverageReport")

            val outputDir = File(buildDir, "reports")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            workingDir = rootDir

            val jacocoSubTasks = getReportTasks(tasks.named<JacocoReport>("jacocoMergeReport").get())
            val sources = jacocoSubTasks.flatMap { it.sourceDirectories }
                .map { it.absolutePath }

            doFirst { standardOutput = FileOutputStream(File(outputDir, "cobertura-coverage.xml")) }

            // Convert Jacoco merged coverage report into a Cobertura one, which is supported by Gitlab.
            // See: https://docs.gitlab.com/ee/user/project/merge_requests/test_coverage_visualization.html#java-and-kotlin-examples
            commandLine(
                "python3",
                "$rootDir/plugins/jacoco/scripts/cover2cover.py",
                reportFile.absolutePath,
                *(sources.toTypedArray()),
            )
        }
    }

    private fun Project.configureJacocoTestTask(srcFolder: String = "kotlin") {
        val hasSourceDirs = file("$projectDir/src/main/$srcFolder").exists()

        // Don't setup Jacoco if there are no sources
        if (!hasSourceDirs) return

        plugins.apply(JacocoPlugin::class)
        configure<JacocoPluginExtension> {
            toolVersion = JacocoVersion
        }

        val defaultReportsDir = file("$buildDir/reports/jacoco/jacocoTestReport")
        val reportFile = File(defaultReportsDir, "jacocoTestReport.xml")

        afterEvaluate {
            val jacocoConfig: JacocoReport.() -> Unit = {
                reports {
                    xml.required.set(true)
                    xml.outputLocation.set(reportFile)
                    html.required.set(!project.hasProperty("ci"))
                    html.outputLocation.set(defaultReportsDir)
                }

                val fileFilter = listOf(
                    "**/R.class",
                    "**/R$*.class",
                    "**/BuildConfig.*",
                    "**/Manifest*.*",
                    "**/*Test*.*",
                    "android/**/*.*",
                    "ch.protonmail.android.utils.nativelib",
                    "**/ch/protonmail/**",
                    // DI code, doesn't need testing
                    "**/*Module.class",
                    "**/*Module$*",
                    // androidTest code, shouldn't have coverage
                    "**/me/proton/core/test/**",
                    // Room Dao classes don't need testing
                    "**/*Dao.class",
                    // Migrations need to be done in instrumented tests which are currently not included in Jacoco reports
                    "**/*MIGRATION*",
                    // These components are tested using UI tests that are currently not included in Jacoco reports
                    "**/*Activity.class",
                    "**/*Activity$*",
                    "**/*Fragment.class",
                    "**/*Fragment$*",
                )

                val debugTree = fileTree("$buildDir/tmp/kotlin-classes/debug") { exclude(fileFilter) }
                val mainSrc = "$projectDir/src/main/$srcFolder"

                sourceDirectories.setFrom(mainSrc)
                classDirectories.setFrom(debugTree)
                executionData.setFrom(fileTree(buildDir) { include(listOf("**/*.exec", "**/*.ec")) })

                dependsOn(tasks.getByName("allTest"))
            }

            tasks.withType<Test> {
                configure<JacocoTaskExtension> {
                    isIncludeNoLocationClasses = true
                    excludes = listOf("jdk.internal.*")
                }
            }

            val isJacocoRegistered = tasks.findByName("jacocoTestReport") != null

            if (!isJacocoRegistered) {
                println("Registering Jacoco for $name")
                tasks.register("jacocoTestReport", jacocoConfig)
            } else {
                println("Configuring registered Jacoco for $name")
                tasks.getByName("jacocoTestReport", jacocoConfig)
            }
        }
    }
}
