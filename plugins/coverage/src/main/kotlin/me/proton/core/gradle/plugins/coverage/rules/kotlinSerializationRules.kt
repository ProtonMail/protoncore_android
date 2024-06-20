package me.proton.core.gradle.plugins.coverage.rules

import kotlinx.kover.gradle.plugin.dsl.KoverReportFiltersConfig

internal fun KoverReportFiltersConfig.kotlinSerializationRules() {
    excludes {
        annotatedBy(
            "kotlinx.serialization.SerialName",
            "kotlinx.serialization.Serializable"
        )
        classes("*\$\$serializer")
    }
}
