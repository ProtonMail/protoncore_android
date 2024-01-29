package me.proton.core.gradle.plugins.coverage.rules

import kotlinx.kover.gradle.plugin.dsl.KoverReportFilters

internal fun KoverReportFilters.androidRules() {
    excludes {
        annotatedBy("androidx.compose.ui.tooling.preview.Preview")
        classes(
            "*Activity",
            "*Activity\$*",
            "*Binding",
            "*.BuildConfig",
            "*ComposableSingletons\$*",
            "*Fragment",
            "*Fragment\$*",
            "*.R",
            "*.R$*"
        )
    }
}
