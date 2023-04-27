package me.proton.core.gradle.plugins.coverage

import kotlinx.kover.gradle.plugin.KoverGradlePlugin
import kotlinx.kover.gradle.plugin.dsl.KoverNames
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * The plugin can be applied on a separate project.
 * It can generate a combined coverage report from all subprojects
 * which use the [ProtonCoveragePlugin].
 */
public class ProtonGlobalCoveragePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.shouldSkipPluginApplication()) {
            configureGlobalCoverageReports(target)
        }

        target.plugins.apply(ProtonCoveragePlugin::class.java)
    }

    private fun configureGlobalCoverageReports(target: Project) {
        val rootProject = target.rootProject

        rootProject.subprojects {
            if (project == target) return@subprojects
            target.evaluationDependsOn(project.path)
        }

        target.afterEvaluate {
            target.plugins.apply("org.gradle.java-library")
            target.plugins.apply("org.jetbrains.kotlin.jvm")
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
}
