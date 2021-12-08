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

package me.proton.core.reports.domain.entity

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class BugReportValidationTest {
    @Test
    fun `valid bug report`() {
        val errors = BugReport(
            title = "Title",
            description = "Description...",
            username = "username",
            email = "test@user"
        ).validate()
        assertNull(errors)
    }

    @Test
    fun `title and description are missing`() {
        val errors = BugReport(
            title = "",
            description = "",
            username = "",
            email = ""
        ).validate()
        assertNotNull(errors)
        assertEquals(2, errors.size)
        assertTrue(errors.contains(BugReportValidationError.SubjectMissing))
        assertTrue(errors.contains(BugReportValidationError.DescriptionMissing))
    }

    @Test
    fun `blank title and description`() {
        val errors = BugReport(
            title = " ".repeat(20),
            description = " ".repeat(20),
            username = "",
            email = ""
        ).validate()
        assertNotNull(errors)
        assertEquals(2, errors.size)
        assertTrue(errors.contains(BugReportValidationError.SubjectMissing))
        assertTrue(errors.contains(BugReportValidationError.DescriptionMissing))
    }

    @Test
    fun `description is too short`() {
        val errors = BugReport(
            title = "Title",
            description = "Test",
            username = "",
            email = ""
        ).validate()
        assertNotNull(errors)
        assertEquals(1, errors.size)
        assertTrue(errors.contains(BugReportValidationError.DescriptionTooShort))
    }

    @Test
    fun `title and description too long`() {
        val errors = BugReport(
            title = "a".repeat(BugReport.SubjectMaxLength + 1),
            description = "b".repeat(BugReport.DescriptionMaxLength + 1),
            username = "",
            email = ""
        ).validate()
        assertNotNull(errors)
        assertEquals(2, errors.size)
        assertTrue(errors.contains(BugReportValidationError.SubjectTooLong))
        assertTrue(errors.contains(BugReportValidationError.DescriptionTooLong))
    }
}
