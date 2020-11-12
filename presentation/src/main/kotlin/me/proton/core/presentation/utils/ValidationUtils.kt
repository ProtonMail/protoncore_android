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

    private fun validateEmail() = validateNotBlank()
}

inline fun InputValidationResult.onFailure(action: () -> Unit): InputValidationResult {
    if (!isValid) action()
    return this
}

inline fun InputValidationResult.onSuccess(action: (text: String) -> Unit): InputValidationResult {
    if (isValid) action(text)
    return this
}
