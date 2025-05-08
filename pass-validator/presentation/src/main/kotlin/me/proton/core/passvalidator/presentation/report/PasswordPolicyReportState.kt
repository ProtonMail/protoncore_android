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

public sealed interface PasswordPolicyReportState {
    public data object Loading : PasswordPolicyReportState
    public data object Hidden : PasswordPolicyReportState
    public data class Idle(
        val messages: List<PasswordPolicyReportMessage>
    ) : PasswordPolicyReportState
}

public sealed interface PasswordPolicyReportMessage {
    public data class Error(val message: String) : PasswordPolicyReportMessage
    public data class Requirement(val message: String, val success: Boolean) : PasswordPolicyReportMessage
}

internal fun PasswordPolicyReportState.allPassed(): Boolean = when (this) {
    is PasswordPolicyReportState.Loading -> false
    is PasswordPolicyReportState.Hidden -> true
    is PasswordPolicyReportState.Idle -> messages.all { it is PasswordPolicyReportMessage.Requirement && it.success }
}
