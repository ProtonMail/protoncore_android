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

import me.proton.core.presentation.ui.view.ProtonInput

fun ProtonInput.validate(validationType: ValidationType = ValidationType.NotBlank) =
    InputValidationResult(this, validationType)

fun ProtonInput.validateUsername() =
    InputValidationResult(this, ValidationType.Username)

fun ProtonInput.validatePassword() =
    InputValidationResult(this, ValidationType.Password)

fun ProtonInput.validateEmail() =
    InputValidationResult(this, ValidationType.Email)

enum class ValidationType {
    NotBlank,
    Username,
    Password,
    Email,
}

data class InputValidationResult(
    val input: ProtonInput,
    val validationType: ValidationType = ValidationType.NotBlank
) {
    val text = input.text.toString()
    val isValid = when (validationType) {
        ValidationType.NotBlank -> validateNotBlank()
        ValidationType.Username -> validateUsername()
        ValidationType.Password -> validatePassword()
        ValidationType.Email -> validateEmail()
    }

    private fun validateNotBlank() = text.isNotBlank()

    private fun validateUsername() = validateNotBlank()

    private fun validatePassword() = validateNotBlank()

    private fun validateEmail(): Boolean {
        val regex = VALIDATION_PATTERN.toRegex(RegexOption.IGNORE_CASE)
        return validateNotBlank() && regex.matches(text)
    }

    companion object {
        const val VALIDATION_PATTERN = """(?:[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#${'$'}%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"""
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
