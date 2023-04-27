package me.proton.core.gradle.plugins.coverage.rules

import kotlinx.kover.gradle.plugin.dsl.KoverReportFilters

internal fun KoverReportFilters.kotlinParcelizeRules() {
    excludes {
        annotatedBy("kotlinx.parcelize.Parcelize")
    }
}
