/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.report.domain.entity

import kotlinx.serialization.Serializable

@Serializable
public data class BugReport(
    val title: String,
    val description: String,
    val username: String,
    val email: String
) {
    public companion object {
        public const val DescriptionMinLength: Int = 10
        public const val DescriptionMaxLength: Int = 1000
        public const val SubjectMaxLength: Int = 100
    }
}

public fun BugReport.validate(): List<BugReportValidationError>? {
    val list = mutableListOf<BugReportValidationError>()

    if (title.isBlank()) {
        list.add(BugReportValidationError.SubjectMissing)
    } else if (title.length > BugReport.SubjectMaxLength) {
        list.add(BugReportValidationError.SubjectTooLong)
    }

    when {
        description.isBlank() -> list.add(BugReportValidationError.DescriptionMissing)
        description.length < BugReport.DescriptionMinLength -> list.add(BugReportValidationError.DescriptionTooShort)
        description.length > BugReport.DescriptionMaxLength -> list.add(BugReportValidationError.DescriptionTooLong)
    }

    return if (list.isEmpty()) null else list
}

public enum class BugReportValidationError {
    SubjectMissing,
    SubjectTooLong,
    DescriptionMissing,
    DescriptionTooLong,
    DescriptionTooShort
}
