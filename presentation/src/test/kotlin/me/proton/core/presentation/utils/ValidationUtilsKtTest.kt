/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.presentation.utils

import androidx.lifecycle.LifecycleOwner
import io.mockk.MockKAnnotations
import io.mockk.declaringKotlinFile
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.presentation.ui.view.ProtonAutoCompleteInput
import me.proton.core.presentation.ui.view.ProtonInput
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationUtilsKtTest {

    @MockK
    private lateinit var protonInput: ProtonInput

    @MockK
    private lateinit var protonAutoCompleteInput: ProtonAutoCompleteInput

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
    }

    @Test
    fun validateCreditCard() {
        every { protonInput.text } returns "1234123412341234"
        var result = protonInput.validateCreditCard()
        assertEquals(ValidationType.CreditCard, result.validationType)
        assertEquals("1234123412341234", result.text)
        assertTrue(result.isValid)

        every { protonInput.text } returns "123412341234"
        result = protonInput.validateCreditCard()
        assertEquals(ValidationType.CreditCard, result.validationType)
        assertEquals("123412341234", result.text)
        assertFalse(result.isValid)

        every { protonInput.text } returns "123412341234123412"
        result = protonInput.validateCreditCard()
        assertEquals(ValidationType.CreditCard, result.validationType)
        assertEquals("123412341234123412", result.text)
        assertFalse(result.isValid)

        every { protonInput.text } returns null
        result = protonInput.validateCreditCard()
        assertEquals(ValidationType.CreditCard, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)
    }

    @Test
    fun validateCreditCardCvc() {
        every { protonInput.text } returns "123"
        var result = protonInput.validateCreditCardCVC()
        assertEquals(ValidationType.CreditCardCVC, result.validationType)
        assertEquals("123", result.text)
        assertTrue(result.isValid)

        every { protonInput.text } returns "1234"
        result = protonInput.validateCreditCardCVC()
        assertEquals(ValidationType.CreditCardCVC, result.validationType)
        assertEquals("1234", result.text)
        assertTrue(result.isValid)

        every { protonInput.text } returns "12345"
        result = protonInput.validateCreditCardCVC()
        assertEquals(ValidationType.CreditCardCVC, result.validationType)
        assertEquals("12345", result.text)
        assertFalse(result.isValid)

        every { protonInput.text } returns null
        result = protonInput.validateCreditCardCVC()
        assertEquals(ValidationType.CreditCardCVC, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)
    }

    @Test
    fun validateExpirationDate() {
        every { protonInput.text } returns "12/30"
        var result = protonInput.validateExpirationDate()
        assertEquals(ValidationType.CreditCardExpirationDate, result.validationType)
        assertEquals("12/30", result.text)
        assertTrue(result.isValid)

        every { protonInput.text } returns "12/22"
        result = protonInput.validateExpirationDate()
        assertEquals(ValidationType.CreditCardExpirationDate, result.validationType)
        assertEquals("12/22", result.text)
        assertFalse(result.isValid)

        every { protonInput.text } returns "1234"
        result = protonInput.validateExpirationDate()
        assertEquals(ValidationType.CreditCardExpirationDate, result.validationType)
        assertEquals("1234", result.text)
        assertFalse(result.isValid)

        every { protonInput.text } returns null
        result = protonInput.validateExpirationDate()
        assertEquals(ValidationType.CreditCardExpirationDate, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)
    }

    @Test
    fun validateUsername() {
        every { protonInput.text } returns "test-username"
        var result = protonInput.validateUsername()
        assertEquals(ValidationType.Username, result.validationType)
        assertEquals("test-username", result.text)
        assertTrue(result.isValid)

        every { protonInput.text } returns ""
        result = protonInput.validateUsername()
        assertEquals(ValidationType.Username, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)

        every { protonInput.text } returns null
        result = protonInput.validateUsername()
        assertEquals(ValidationType.Username, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)
    }

    @Test
    fun validateEmail() {
        every { protonInput.text } returns "test-email@email.com"
        var result = protonInput.validateEmail()
        assertEquals(ValidationType.Email, result.validationType)
        assertEquals("test-email@email.com", result.text)
        assertTrue(result.isValid)

        every { protonInput.text } returns ""
        result = protonInput.validateEmail()
        assertEquals(ValidationType.Email, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)

        every { protonInput.text } returns null
        result = protonInput.validateEmail()
        assertEquals(ValidationType.Email, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)
    }

    @Test
    fun validatePasswordMatch() {
        every { protonInput.text } returns "test-password"
        val confirmPassword = "test-password"
        var result = protonInput.validatePasswordMatch(confirmPassword)
        assertEquals(ValidationType.PasswordMatch, result.validationType)
        assertEquals("test-password", result.text)
        assertEquals(confirmPassword, result.additionalText)
        assertTrue(result.isValid)

        every { protonInput.text } returns ""
        result = protonInput.validatePasswordMatch(confirmPassword)
        assertEquals(ValidationType.PasswordMatch, result.validationType)
        assertEquals("", result.text)
        assertEquals(confirmPassword, result.additionalText)
        assertFalse(result.isValid)

        every { protonInput.text } returns null
        result = protonInput.validatePasswordMatch(confirmPassword)
        assertEquals(ValidationType.PasswordMatch, result.validationType)
        assertEquals("", result.text)
        assertEquals(confirmPassword, result.additionalText)
        assertFalse(result.isValid)

        every { protonInput.text } returns "test-password"
        result = protonInput.validatePasswordMatch("test-password2")
        assertEquals(ValidationType.PasswordMatch, result.validationType)
        assertEquals("test-password", result.text)
        assertEquals("test-password2", result.additionalText)
        assertFalse(result.isValid)
    }

    @Test
    fun validatePasswordMinLength() {
        every { protonInput.text } returns "test-password"
        var result = protonInput.validatePasswordMinLength()
        assertEquals(ValidationType.PasswordMinLength, result.validationType)
        assertEquals("test-password", result.text)
        assertTrue(result.isValid)

        every { protonInput.text } returns ""
        result = protonInput.validatePasswordMinLength()
        assertEquals(ValidationType.PasswordMinLength, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)

        every { protonInput.text } returns null
        result = protonInput.validatePasswordMinLength()
        assertEquals(ValidationType.PasswordMinLength, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)
    }

    @Test
    fun validatePassword() {
        every { protonInput.text } returns "test-password"
        var result = protonInput.validatePassword()
        assertEquals(ValidationType.Password, result.validationType)
        assertEquals("test-password", result.text)
        assertTrue(result.isValid)

        every { protonInput.text } returns ""
        result = protonInput.validatePassword()
        assertEquals(ValidationType.Password, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)

        every { protonInput.text } returns null
        result = protonInput.validatePassword()
        assertEquals(ValidationType.Password, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)
    }

    @Test
    fun validate() {
        every { protonInput.text } returns "test-string"
        var result = protonInput.validate()
        assertEquals(ValidationType.NotBlank, result.validationType)
        assertEquals("test-string", result.text)
        assertTrue(result.isValid)

        every { protonInput.text } returns ""
        result = protonInput.validate()
        assertEquals(ValidationType.NotBlank, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)

        every { protonInput.text } returns null
        result = protonInput.validate()
        assertEquals(ValidationType.NotBlank, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)
    }

    @Test
    fun validateAutoComplete() {
        every { protonAutoCompleteInput.text } returns "test-string"
        var result = protonAutoCompleteInput.validate()
        assertEquals(ValidationType.NotBlank, result.validationType)
        assertEquals("test-string", result.text)
        assertTrue(result.isValid)

        every { protonAutoCompleteInput.text } returns ""
        result = protonAutoCompleteInput.validate()
        assertEquals(ValidationType.NotBlank, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)

        every { protonAutoCompleteInput.text } returns null
        result = protonAutoCompleteInput.validate()
        assertEquals(ValidationType.NotBlank, result.validationType)
        assertEquals("", result.text)
        assertFalse(result.isValid)
    }

    @Test
    fun onFailureInvalid() {
        val actionLambda = mockk<() -> Unit>(relaxed = true)
        every { actionLambda() } returns Unit
        InputValidationResult("", ValidationType.NotBlank).onFailure(actionLambda)
        verify { actionLambda.invoke() }
    }

    @Test
    fun onFailureValid() {
        val actionLambda = mockk<() -> Unit>(relaxed = true)
        every { actionLambda() } returns Unit
        InputValidationResult("123", ValidationType.NotBlank).onFailure(actionLambda)
        verify(exactly = 0) { actionLambda.invoke() }
    }

    @Test
    fun onSuccessValid() {
        val actionLambda = mockk<(String) -> Unit>(relaxed = true)
        every { actionLambda(any()) } returns Unit
        InputValidationResult("123", ValidationType.NotBlank).onSuccess(actionLambda)
        verify { actionLambda.invoke("123") }
    }

    @Test
    fun onSuccessInvalid() {
        val actionLambda = mockk<(String) -> Unit>(relaxed = true)
        every { actionLambda(any()) } returns Unit
        InputValidationResult("", ValidationType.NotBlank).onSuccess(actionLambda)
        verify(exactly = 0) { actionLambda.invoke(any()) }
    }
}