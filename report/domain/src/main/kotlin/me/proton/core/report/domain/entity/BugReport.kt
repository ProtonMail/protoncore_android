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
import me.proton.core.report.domain.entity.BugReport.Companion.validateDescription
import me.proton.core.report.domain.entity.BugReport.Companion.validateTitle

@Serializable
public data class BugReport(
    val title: String,
    val description: String,
    val username: String,
    val email: String,
    val shouldAttachLog: Boolean,
) {
    public companion object {
        public const val DescriptionMinLength: Int = 10
        public const val DescriptionMaxLength: Int = 1000
        public const val SubjectMaxLength: Int = 100

        public fun validateDescription(description: String): List<BugReportValidationError> = buildList {
            when {
                description.isBlank() -> add(BugReportValidationError.DescriptionMissing)
                description.length < DescriptionMinLength -> add(BugReportValidationError.DescriptionTooShort)
                description.length > DescriptionMaxLength -> add(BugReportValidationError.DescriptionTooLong)
            }
        }

        public fun validateTitle(title: String): List<BugReportValidationError> = buildList {
            if (title.isBlank()) {
                add(BugReportValidationError.SubjectMissing)
            } else if (title.length > SubjectMaxLength) {
                add(BugReportValidationError.SubjectTooLong)
            }
        }
    }
}

public fun BugReport.validate(): List<BugReportValidationError> =
    validateTitle(title) + validateDescription(description)

public enum class BugReportField {
    Subject, Description
}

public enum class BugReportValidationError(public val field: BugReportField) {
    SubjectMissing(BugReportField.Subject),
    SubjectTooLong(BugReportField.Subject),
    DescriptionMissing(BugReportField.Description),
    DescriptionTooLong(BugReportField.Description),
    DescriptionTooShort(BugReportField.Description)
}
