/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.gradle.plugins.coverage

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.Node
import kotlinx.kover.gradle.plugin.KoverGradlePlugin
import kotlinx.kover.gradle.plugin.dsl.KoverNames
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.util.Locale

internal const val globalLineCoverageTaskName = "globalLineCoverage"

/**
 * The plugin can be applied on a separate project.
 * It can generate a combined coverage report from all subprojects
 * which use the [ProtonCoveragePlugin].
 */
public class ProtonGlobalCoveragePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.shouldSkipPluginApplication()) {
            configureGlobalCoverageReports(target)
            registerGlobalLineCoverageTask(target)
        }

        target.plugins.apply(ProtonCoveragePlugin::class.java)
    }

    private fun configureGlobalCoverageReports(target: Project) {
        val rootProject = target.rootProject

        rootProject.subprojects {
            if (project == target) return@subprojects
            target.evaluationDependsOn(project.path)
        }

        target.plugins.apply(PluginIds.javaLibrary)
        target.plugins.apply(PluginIds.kotlinJvm)

        target.afterEvaluate {
            target.plugins.apply(KoverGradlePlugin::class.java)

            target.extensions.configure<ProtonCoverageExtension> {
                enableAllRules.set(true)
            }

            rootProject.subprojects {
                if (project == target) return@subprojects

                if (project.plugins.hasPlugin(ProtonCoveragePlugin::class.java) &&
                    project.extensions.findByType(ProtonCoverageExtension::class.java)?.disabled?.get() != true
                ) {
                    target.dependencies.add(KoverNames.DEPENDENCY_CONFIGURATION_NAME, project)
                }
            }
        }
    }

    /** Registers a task that prints out the total line coverage. */
    private fun registerGlobalLineCoverageTask(target: Project) {
        target.tasks.register(globalLineCoverageTaskName) {
            dependsOn(target.tasks.named(KoverNames.DEFAULT_XML_REPORT_NAME))
            description = "Prints out total line coverage percentage."

            doLast {
                val xmlReportFile =
                    project.layout.buildDirectory.asFile.get().resolve(DEFAULT_XML_REPORT_FILE)
                val xmlReport = XmlSlurper().parse(xmlReportFile)
                val counterNode =
                    xmlReport.childNodes().asSequence().filterIsInstance<Node>().firstOrNull {
                        it.name() == "counter" && it.attributes()["type"] == "LINE"
                    } ?: run {
                        target.logger.warn("Could not obtain total line coverage: `counter` node was not found.")
                        return@doLast
                    }
                val missed = (counterNode.attributes()["missed"] as String).toFloat()
                val covered = (counterNode.attributes()["covered"] as String).toFloat()
                val total = missed + covered
                val percentage = covered / total * 100.0f
                println("TotalLineCoverage: %.2f%%".format(Locale.US, percentage))
            }
        }
    }
}
