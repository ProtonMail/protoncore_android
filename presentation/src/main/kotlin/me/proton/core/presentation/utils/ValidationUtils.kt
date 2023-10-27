/*
 * Copyright (c) 2020 Proton Technologies AG
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

import me.proton.core.presentation.ui.view.ProtonAutoCompleteInput
import me.proton.core.presentation.ui.view.ProtonInput
import me.proton.core.util.kotlin.exhaustive
import java.util.Calendar

fun ProtonInput.validate(validationType: ValidationType = ValidationType.NotBlank) =
    InputValidationResult(this.text.toString(), validationType)

fun ProtonAutoCompleteInput.validate(validationType: ValidationType = ValidationType.NotBlank) =
    InputValidationResult(this.text.toString(), validationType)

fun ProtonInput.validateUsername() =
    InputValidationResult(this.text.toString(), ValidationType.Username)

fun ProtonInput.validatePassword() =
    InputValidationResult(this.text.toString(), ValidationType.Password)

fun ProtonInput.validatePasswordMinLength() =
    InputValidationResult(this.text.toString(), ValidationType.PasswordMinLength)

fun ProtonInput.validatePasswordMatch(confirmPassword: String) =
    InputValidationResult(this.text.toString(), ValidationType.PasswordMinLength, confirmPassword)

fun ProtonInput.validateEmail() =
    InputValidationResult(this.text.toString(), ValidationType.Email)

fun ProtonInput.validateCreditCard() =
    InputValidationResult(this.text.toString(), ValidationType.CreditCard)

fun ProtonInput.validateCreditCardCVC() =
    InputValidationResult(this.text.toString(), ValidationType.CreditCardCVC)

fun ProtonInput.validateExpirationDate() =
    InputValidationResult(this.text.toString(), ValidationType.CreditCardExpirationDate)

enum class ValidationType(val minLong: Int = Int.MIN_VALUE, val maxLong: Int = Int.MAX_VALUE) {
    NotBlank,
    Username,
    Password,
    PasswordMinLength(8),
    PasswordMatch,
    Email,
    CreditCard,
    CreditCardCVC(minLong = 3, maxLong = 4),
    CreditCardExpirationDate
}

enum class CardType(val regex: String) {
    VISA("^4[0-9]{12}(?:[0-9]{3})?"),
    MASTERCARD("^5[1-5][0-9]{14}|^(222[1-9]|22[3-9]\\\\d|2[3-6]\\\\d{2}|27[0-1]\\\\d|2720)[0-9]{12}"),
    AMEX("^3[47][0-9]{13}"),
    DISCOVER("^6(?:011|5[0-9]{2})[0-9]{12}"),
    OTHER("\\b\\d{13,16}\\b")
}

data class InputValidationResult(
    val text: String,
    val validationType: ValidationType = ValidationType.NotBlank,
    val additionalText: String? = null
) {
    var cardType: CardType? = null

    val isValid = when (validationType) {
        ValidationType.NotBlank -> validateNotBlank()
        ValidationType.Username -> validateUsername()
        ValidationType.Password -> validatePassword()
        ValidationType.PasswordMinLength -> validatePasswordMinLength(validationType.minLong)
        ValidationType.PasswordMatch -> validateNotBlank() && text == additionalText
        ValidationType.Email -> validateEmail()
        ValidationType.CreditCard -> {
            cardType = validateCreditCard()
            cardType != null
        }
        ValidationType.CreditCardCVC ->
            validateNotBlankMinLong(minLong = validationType.minLong) &&
                validateNotBlankMaxLong(maxLong = validationType.maxLong)
        ValidationType.CreditCardExpirationDate -> validateCreditCardExpirationDate(Calendar.getInstance())
    }.exhaustive

    private fun validateNotBlank() = text.isNotBlank()

    private fun validateNotBlankMinLong(minLong: Int) = text.isNotBlank() && text.length >= minLong

    private fun validateNotBlankMaxLong(maxLong: Int) = text.isNotBlank() && text.length <= maxLong

    private fun validateNotBlankAndAtLeastLong(chars: Int) = text.isNotBlank() && text.length >= chars

    private fun validateUsername() = validateNotBlank()

    private fun validatePassword() = validateNotBlank()

    private fun validatePasswordMinLength(length: Int) = validateNotBlankAndAtLeastLong(length)

    private fun validateEmail(): Boolean {
        val regex = EMAIL_VALIDATION_PATTERN.toRegex(RegexOption.IGNORE_CASE)
        return validateNotBlank() && regex.matches(text)
    }

    private fun validateCreditCard(): CardType? {
        val input = text.replace(" ", "")
        val types = CardType.values().map {
            it.ordinal to it.regex.toRegex(RegexOption.IGNORE_CASE).matches(input)
        }.toMap()

        val key = types.entries.firstOrNull { it.value }?.key
        return key?.let {
            CardType.values()[it]
        } ?: run { null }
    }

    private fun validateCreditCardExpirationDate(calendar: Calendar): Boolean {
        val monthAndYear = text.split("/")
        if (monthAndYear.size != 2) {
            return false
        }
        return try {
            val month = monthAndYear[0].toInt()
            val year = monthAndYear[1].toInt()
            val currentYear = calendar.get(Calendar.YEAR) - YEAR_2000
            val currentMonth = calendar.get(Calendar.MONTH) + 1 // it is 0-based
            val minMonth = if (year == currentYear) currentMonth else MIN_MONTH

            month in minMonth..MAX_MONTH && year in currentYear..MAX_YEAR
        } catch (numberFormat: NumberFormatException) {
            false
        }
    }

    companion object {
        const val EMAIL_VALIDATION_PATTERN =
            """(?:[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"""
        const val YEAR_2000 = 2000
        const val MAX_YEAR = 99
        const val MIN_MONTH = 1
        const val MAX_MONTH = 12
    }
}

inline fun InputValidationResult.onFailure(action: () -> Unit): InputValidationResult {
    if (!isValid) action()
    return this
}

inline fun InputValidationResult.onSuccess(action: (text: String) -> Unit): InputValidationResult {
    if (isValid) action(text)
    return this
}
