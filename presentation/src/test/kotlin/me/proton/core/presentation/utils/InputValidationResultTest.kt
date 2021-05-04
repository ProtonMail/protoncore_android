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

package me.proton.core.presentation.utils

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import me.proton.core.test.kotlin.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputValidationResultTest {

    private val calendar = mockk<Calendar>(relaxed = true)

    @Before
    fun beforeEveryTest() {
        mockkStatic(Calendar::class)
    }

    @Test
    fun `validate CreditCard exp date in future`() {
        // GIVEN
        val inputText = "10/22"
        every { Calendar.getInstance() } returns calendar
        every { calendar.get(Calendar.YEAR) } returns 2021
        every { calendar.get(Calendar.MONTH) } returns 4 // zero based
        // WHEN
        val inputValidationResult = InputValidationResult(inputText, ValidationType.CreditCardExpirationDate)
        val result = inputValidationResult.isValid
        // THEN
        assertTrue(result)
    }

    @Test
    fun `validate CreditCard exp date this month`() {
        // GIVEN
        val inputText = "05/21"
        every { Calendar.getInstance() } returns calendar
        every { calendar.get(Calendar.YEAR) } returns 2021
        every { calendar.get(Calendar.MONTH) } returns 4 // zero based
        // WHEN
        val inputValidationResult = InputValidationResult(inputText, ValidationType.CreditCardExpirationDate)
        val result = inputValidationResult.isValid
        // THEN
        assertTrue(result)
    }

    @Test
    fun `validate CreditCard exp date in past previous month this year`() {
        // GIVEN
        val inputText = "04/21"
        every { Calendar.getInstance() } returns calendar
        every { calendar.get(Calendar.YEAR) } returns 2021
        every { calendar.get(Calendar.MONTH) } returns 4 // zero based
        // WHEN
        val inputValidationResult = InputValidationResult(inputText, ValidationType.CreditCardExpirationDate)
        val result = inputValidationResult.isValid
        // THEN
        assertFalse(result)
    }

    @Test
    fun `validate CreditCard exp date in past previous year`() {
        // GIVEN
        val inputText = "12/20"
        every { Calendar.getInstance() } returns calendar
        every { calendar.get(Calendar.YEAR) } returns 2021
        every { calendar.get(Calendar.MONTH) } returns 4 // zero based
        // WHEN
        val inputValidationResult = InputValidationResult(inputText, ValidationType.CreditCardExpirationDate)
        val result = inputValidationResult.isValid
        // THEN
        assertFalse(result)
    }

    @Test
    fun `validate CreditCard CVC short 2 digits invalid`() {
        // GIVEN
        val inputText = "12"
        // WHEN
        val inputValidationResult = InputValidationResult(inputText, ValidationType.CreditCardCVC)
        val result = inputValidationResult.isValid
        // THEN
        assertFalse(result)
    }

    @Test
    fun `validate CreditCard CVC empty invalid`() {
        // GIVEN
        val inputText = ""
        // WHEN
        val inputValidationResult = InputValidationResult(inputText, ValidationType.CreditCardCVC)
        val result = inputValidationResult.isValid
        // THEN
        assertFalse(result)
    }

    @Test
    fun `validate CreditCard CVC 3 digits valid`() {
        // GIVEN
        val inputText = "123"
        // WHEN
        val inputValidationResult = InputValidationResult(inputText, ValidationType.CreditCardCVC)
        val result = inputValidationResult.isValid
        // THEN
        assertTrue(result)
    }

    @Test
    fun `validate CreditCard CVC 4 digits valid`() {
        // GIVEN
        val inputText = "1234"
        // WHEN
        val inputValidationResult = InputValidationResult(inputText, ValidationType.CreditCardCVC)
        val result = inputValidationResult.isValid
        // THEN
        assertTrue(result)
    }

    @Test
    fun `validate CreditCard CVC 5 digits invalid`() {
        // GIVEN
        val inputText = "12345"
        // WHEN
        val inputValidationResult = InputValidationResult(inputText, ValidationType.CreditCardCVC)
        val result = inputValidationResult.isValid
        // THEN
        assertFalse(result)
    }

    @After
    fun afterEveryTest() {
        unmockkStatic(Calendar::class)
    }
}
