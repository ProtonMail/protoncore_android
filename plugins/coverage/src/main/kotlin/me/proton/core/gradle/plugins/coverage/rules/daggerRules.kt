package me.proton.core.gradle.plugins.coverage.rules

import kotlinx.kover.gradle.plugin.dsl.KoverReportFilters

internal fun KoverReportFilters.daggerRules() {
    excludes {
        annotatedBy(
            "dagger.hilt.android.AndroidEntryPoint",
            "dagger.internal.DaggerGenerated",
            "dagger.Binds",
            "dagger.Module",
            "dagger.Provides",
            "javax.annotation.processing.Generated"
        )
        classes(
            "*Hilt_*",
            "*_HiltModules_*",
            "*_Provide*Factory",
            "*_Provide*Factory\$*",
            "*_Factory"
        )
        packages("hilt_aggregated_deps")
    }
}
