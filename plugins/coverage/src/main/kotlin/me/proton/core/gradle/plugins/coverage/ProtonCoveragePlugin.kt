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

import kotlinx.kover.gradle.plugin.KoverGradlePlugin
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportFilters
import kotlinx.kover.gradle.plugin.dsl.KoverVerifyReportConfig
import kotlinx.kover.gradle.plugin.dsl.MetricType
import me.proton.core.gradle.plugins.coverage.rules.androidRules
import me.proton.core.gradle.plugins.coverage.rules.daggerRules
import me.proton.core.gradle.plugins.coverage.rules.kotlinParcelizeRules
import me.proton.core.gradle.plugins.coverage.rules.kotlinSerializationRules
import me.proton.core.gradle.plugins.coverage.rules.roomDbRules
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

/** Default dir path for the HTML report, relative to the build directory. */
private const val DEFAULT_HTML_REPORT_DIR = "reports/kover/html"

/** Default file path for the XML report, relative to the build directory. */
internal const val DEFAULT_XML_REPORT_FILE = "reports/kover/report.xml"

internal const val PROTON_COVERAGE_EXT: String = "protonCoverage"

/**
 * The plugin can be applied on a non-root project.
 * It can be used to generate coverage HTML/XML report, and verify minimum coverage percentages.
 */
public class ProtonCoveragePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target == target.rootProject) error("${this::class.simpleName} should not be applied on the root project.")

        val ext = target.extensions.create<ProtonCoverageExtension>(PROTON_COVERAGE_EXT)

        if (target.shouldSkipPluginApplication()) {
            return
        }

        ext.applyGeneralConventions()
        ext.applyConventionsFrom(target.rootProject)
        ext.finalizeValuesOnRead()

        target.afterEvaluate {
            if (ext.disabled.get()) return@afterEvaluate
            target.pluginManager.apply(KoverGradlePlugin::class.java)
            onAfterEvaluate(ext)
        }
    }

    private fun Project.onAfterEvaluate(ext: ProtonCoverageExtension) {
        extensions.configure<KoverProjectExtension> {
            useKoverTool()
        }

        if (hasAndroidPlugin()) {
            ext.androidBuildVariant.convention(DEFAULT_ANDROID_BUILD_VARIANT)
            ext.enableAndroidRules.convention(true)
            ext.enableRoomDbRules.convention(true)
        }
        if (plugins.hasPlugin(PluginIds.hilt)) {
            ext.enableDaggerRules.convention(true)
        }
        if (plugins.hasPlugin(PluginIds.kotlinParcelize)) {
            ext.enableKotlinParcelizeRules.convention(true)
        }
        if (plugins.hasPlugin(PluginIds.kotlinSerialization)) {
            ext.enableKotlinSerializationRules.convention(true)
        }
        if (ext.enableAllRules.orNull == true) {
            ext.enableAndroidRules.convention(true)
            ext.enableDaggerRules.convention(true)
            ext.enableKotlinParcelizeRules.convention(true)
            ext.enableKotlinSerializationRules.convention(true)
            ext.enableRoomDbRules.convention(true)
        }

        configureKoverExtension(ext)
    }

    private fun Project.hasAndroidPlugin(): Boolean =
        plugins.hasPlugin(PluginIds.androidApp) || plugins.hasPlugin(PluginIds.androidLibrary)

    private fun Project.configureKoverExtension(ext: ProtonCoverageExtension) {
        extensions.configure<KoverReportExtension> {
            defaults {
                ext.androidBuildVariant.orNull?.let { mergeWith(it) }
                html {
                    setReportDir(layout.buildDirectory.dir(DEFAULT_HTML_REPORT_DIR))
                }
                xml {
                    setReportFile(layout.buildDirectory.file(DEFAULT_XML_REPORT_FILE))
                }
                verify {
                    applyVerificationConfig(ext)
                }
            }
            filters {
                applyFiltersConfig(ext)
            }
        }
    }

    private fun KoverReportFilters.applyFiltersConfig(ext: ProtonCoverageExtension) {
        if (ext.enableAndroidRules.orNull == true) androidRules()
        if (ext.enableDaggerRules.orNull == true) daggerRules()
        if (ext.enableKotlinParcelizeRules.orNull == true) kotlinParcelizeRules()
        if (ext.enableKotlinSerializationRules.orNull == true) kotlinSerializationRules()
        if (ext.enableRoomDbRules.orNull == true) roomDbRules()
        ext.excludes.get().forEach {
            excludes(it)
        }
    }

    private fun KoverVerifyReportConfig.applyVerificationConfig(ext: ProtonCoverageExtension) {
        rule("minBranchCoveragePercentage") {
            minBound(
                ext.minBranchCoveragePercentage.get(),
                MetricType.BRANCH,
                AggregationType.COVERED_PERCENTAGE
            )
        }
        rule("minLineCoveragePercentage") {
            minBound(
                ext.minLineCoveragePercentage.get(),
                MetricType.LINE,
                AggregationType.COVERED_PERCENTAGE
            )
        }
    }
}
