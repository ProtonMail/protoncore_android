package me.proton.core.gradle.plugins.coverage.rules

import kotlinx.kover.gradle.plugin.dsl.KoverReportFiltersConfig

internal fun KoverReportFiltersConfig.androidRules() {
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
