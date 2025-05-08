/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.passvalidator.data.validator

import android.content.Context
import me.proton.core.passvalidator.domain.entity.PasswordValidatorResult
import me.proton.core.passvalidator.data.R

internal class CommonPasswordValidator(
    private val context: Context,
    private val hideIfValid: Boolean,
    private val isPasswordCommon: (password: String) -> Boolean,
    private val isOptional: Boolean
) : PasswordValidator {
    override fun validate(password: String) = PasswordValidatorResult(
        errorMessage = context.getString(R.string.password_validator_common_error),
        hideIfValid = hideIfValid,
        isOptional = isOptional,
        isValid = !isPasswordCommon(password),
        requirementMessage = context.getString(R.string.password_validator_common_requirement)
    )
}
