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

package me.proton.core.passvalidator.presentation.report

import me.proton.core.passvalidator.domain.entity.PasswordValidatorToken

public sealed class PasswordPolicyReportState(public open val token: PasswordValidatorToken?) {
    public data object Loading : PasswordPolicyReportState(token = null)
    public data class Hidden(override val token: PasswordValidatorToken?) : PasswordPolicyReportState(token)
    public data class Idle(
        val messages: List<PasswordPolicyReportMessage>,
        override val token: PasswordValidatorToken?
    ) : PasswordPolicyReportState(token)
}

public sealed class PasswordPolicyReportMessage(public open val success: Boolean) {
    public data class Error(
        val message: String
    ) : PasswordPolicyReportMessage(success = false)

    public data class Hint(
        val message: String,
        override val success: Boolean,
    ) : PasswordPolicyReportMessage(success = success)

    public data class Requirement(
        val message: String,
        override val success: Boolean
    ) : PasswordPolicyReportMessage(success = success)
}
