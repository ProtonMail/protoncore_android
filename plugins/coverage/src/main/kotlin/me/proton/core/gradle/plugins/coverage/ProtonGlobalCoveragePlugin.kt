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
import kotlinx.kover.gradle.plugin.dsl.KoverNames
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Locale

internal const val GLOBAL_LINE_COVERAGE_TASK_NAME = "globalLineCoverage"
internal const val DEFAULT_REPORT_VARIANT_NAME = "default"
internal val XML_REPORT_NAME = KoverNames.koverXmlReportName + DEFAULT_REPORT_VARIANT_NAME.replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(
        Locale.getDefault()
    ) else it.toString()
}

/**
 * The plugin can be applied on a separate project.
 * It can generate a combined coverage report from all subprojects
 * which use the [ProtonCoveragePlugin].
 */
public class ProtonGlobalCoveragePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(ProtonCoveragePlugin::class.java)
        if (!target.shouldSkipPluginApplication()) {
            configureGlobalCoverageReports(target)
            registerGlobalLineCoverageTask(target)
        }
    }

    private fun configureGlobalCoverageReports(target: Project) {
        val rootProject = target.rootProject

        rootProject.subprojects {
            if (project == target) return@subprojects
            target.evaluationDependsOn(project.path)
        }

        target.afterEvaluate {
            rootProject.subprojects {
                if (project == target) return@subprojects

                if (project.plugins.hasPlugin(ProtonCoveragePlugin::class.java)
                    && project.extensions.findByType(ProtonCoverageExtension::class.java)?.disabled?.get() != true
                ) {
                    target.dependencies.add(KoverNames.configurationName, project)
                }
            }
        }
    }

    /** Registers a task that prints out the total line coverage. */
    private fun registerGlobalLineCoverageTask(target: Project) {
        val globalLineCoverageTask = target.tasks.register(GLOBAL_LINE_COVERAGE_TASK_NAME) {
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

        target.afterEvaluate {
            globalLineCoverageTask.configure {
                dependsOn(target.tasks.named(XML_REPORT_NAME))
            }
        }
    }
}
