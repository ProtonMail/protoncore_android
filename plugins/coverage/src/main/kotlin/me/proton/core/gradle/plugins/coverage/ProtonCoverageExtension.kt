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

import kotlinx.kover.gradle.plugin.dsl.KoverReportFilter
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

internal const val DEFAULT_ANDROID_BUILD_VARIANT = "debug"
private const val DEFAULT_MIN_BRANCH_PERCENTAGE_COVERAGE = 90
private const val DEFAULT_MIN_LINE_PERCENTAGE_COVERAGE = 90

public interface ProtonCoverageExtension {
    /** The Android build variant to use for the default report. Defaults to `debug`. */
    public val androidBuildVariant: Property<String>

    /** Set to `true` to disable code coverage for the project. By default, coverage is enabled. */
    public val disabled: Property<Boolean>

    /** Enable all custom rules. May be useful when aggregating coverage reports. */
    public val enableAllRules: Property<Boolean>

    /** Explicitly enable Android rules. By default, they are enabled if an Android plugin is applied. */
    public val enableAndroidRules: Property<Boolean>

    /** Explicitly enable Dagger rules. By default, they are enabled if the Dagger plugin is applied. */
    public val enableDaggerRules: Property<Boolean>

    /** Explicitly enable Kotlin Parcelize rules. By default, they are enabled if the Kotlin Parcelize plugin is applied. */
    public val enableKotlinParcelizeRules: Property<Boolean>

    /** Explicitly enable Kotlin Serialization rules. By default, they are enabled if the Kotlin Serialization plugin is applied. */
    public val enableKotlinSerializationRules: Property<Boolean>

    /** Explicitly enable Room DB rules. By default, they are enabled if an Android plugin is applied. */
    public val enableRoomDbRules: Property<Boolean>

    /** A list of extra filters used to exclude some code from coverage.
     * Can be called multiple times by invoking [excludes.add][ListProperty.add].
     */
    public val excludes: ListProperty<KoverReportFilter.() -> Unit>

    /** The branch coverage percentage (0-100) that the project must meet (+/- 1%).
     * Use only if you want to override a default value.
     */
    public val branchCoveragePercentage: Property<Int>

    /** The line coverage percentage (0-100) that the project must meet (+/- 1%).
     * Use only if you want to override a default value.
     */
    public val lineCoveragePercentage: Property<Int>

    /** Explicitly enable Common rules. By default, they are enabled. */
    public val enableCommonRules: Property<Boolean>
}

internal fun ProtonCoverageExtension.applyConventionsFrom(project: Project) {
    project.extensions.findByType(ProtonCoverageExtension::class.java)?.let { otherExt ->
        require(otherExt != this) { "Cannot apply the convention from the same object." }
        applyConventionsFrom(otherExt)
    }
}

internal fun ProtonCoverageExtension.applyConventionsFrom(other: ProtonCoverageExtension) {
    other.androidBuildVariant.orNull?.let { androidBuildVariant.convention(it) }
    other.disabled.orNull?.let { disabled.convention(it) }
    other.enableAllRules.orNull?.let { enableAllRules.convention(it) }
    other.enableAndroidRules.orNull?.let { enableAndroidRules.convention(it) }
    other.enableDaggerRules.orNull?.let { enableDaggerRules.convention(it) }
    other.enableKotlinParcelizeRules.orNull?.let { enableKotlinParcelizeRules.convention(it) }
    other.enableKotlinSerializationRules.orNull?.let { enableKotlinSerializationRules.convention(it) }
    other.enableRoomDbRules.orNull?.let { enableRoomDbRules.convention(it) }
    other.excludes.orNull?.let { excludes.convention(it) }
    other.branchCoveragePercentage.orNull?.let { branchCoveragePercentage.convention(it) }
    other.lineCoveragePercentage.orNull?.let { lineCoveragePercentage.convention(it) }
    other.enableCommonRules.orNull?.let { enableCommonRules.convention(it) }
}

internal fun ProtonCoverageExtension.applyGeneralConventions() {
    disabled.convention(false)
    excludes.convention(emptyList())
    branchCoveragePercentage.convention(DEFAULT_MIN_BRANCH_PERCENTAGE_COVERAGE)
    lineCoveragePercentage.convention(DEFAULT_MIN_LINE_PERCENTAGE_COVERAGE)
}

internal fun ProtonCoverageExtension.finalizeValuesOnRead() {
    androidBuildVariant.finalizeValueOnRead()
    disabled.finalizeValueOnRead()
    enableAllRules.finalizeValueOnRead()
    enableAndroidRules.finalizeValueOnRead()
    enableDaggerRules.finalizeValueOnRead()
    enableKotlinParcelizeRules.finalizeValueOnRead()
    enableKotlinSerializationRules.finalizeValueOnRead()
    enableRoomDbRules.finalizeValueOnRead()
    excludes.finalizeValueOnRead()
    branchCoveragePercentage.finalizeValueOnRead()
    lineCoveragePercentage.finalizeValueOnRead()
}
