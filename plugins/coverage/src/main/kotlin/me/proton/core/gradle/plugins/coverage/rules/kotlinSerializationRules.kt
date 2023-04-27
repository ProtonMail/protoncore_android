package me.proton.core.gradle.plugins.coverage.rules

import kotlinx.kover.gradle.plugin.dsl.KoverReportFilters

internal fun KoverReportFilters.kotlinSerializationRules() {
    excludes {
        annotatedBy(
            "kotlinx.serialization.SerialName",
            "kotlinx.serialization.Serializable"
        )
        classes("*\$\$serializer")
    }
}
