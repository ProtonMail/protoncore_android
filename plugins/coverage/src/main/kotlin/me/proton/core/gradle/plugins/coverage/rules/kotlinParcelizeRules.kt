package me.proton.core.gradle.plugins.coverage.rules

import kotlinx.kover.gradle.plugin.dsl.KoverReportFiltersConfig

internal fun KoverReportFiltersConfig.kotlinParcelizeRules() {
    excludes {
        annotatedBy("kotlinx.parcelize.Parcelize")
    }
}
