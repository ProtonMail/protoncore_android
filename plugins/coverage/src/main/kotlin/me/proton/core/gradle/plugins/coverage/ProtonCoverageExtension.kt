/*
 * Copyright (c) 2023 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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
import org.gradle.api.provider.Property

private const val DEFAULT_MIN_BRANCH_PERCENTAGE_COVERAGE = 90
private const val DEFAULT_MIN_LINE_PERCENTAGE_COVERAGE = 90

public interface ProtonCoverageExtension {
    public val disabledForProject: Property<Boolean>
    public val excludes: Property<KoverReportFilter.() -> Unit>
    public val minBranchCoveragePercentage: Property<Int>
    public val minLineCoveragePercentage: Property<Int>
}

internal fun ProtonCoverageExtension.applyConventions() {
    disabledForProject.convention(false)
    minBranchCoveragePercentage.convention(DEFAULT_MIN_BRANCH_PERCENTAGE_COVERAGE)
    minLineCoveragePercentage.convention(DEFAULT_MIN_LINE_PERCENTAGE_COVERAGE)
}
