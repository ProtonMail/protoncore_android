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

package me.proton.core.passvalidator.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.domain.type.IntEnum
import me.proton.core.passvalidator.data.LogTag
import me.proton.core.passvalidator.data.entity.PasswordPolicy
import me.proton.core.passvalidator.data.entity.PasswordPolicyState
import me.proton.core.util.kotlin.CoreLogger
import java.util.regex.PatternSyntaxException

@Serializable
internal data class PasswordPoliciesResponse(
    @SerialName("PasswordPolicies")
    val passwordPolicies: List<PasswordPolicyResource>
)

@Serializable
internal data class PasswordPolicyResource(
    @SerialName("PolicyName")
    val name: String,

    @SerialName("State")
    val state: Int,

    @SerialName("RequirementMessage")
    val requirementMessage: String,

    @SerialName("ErrorMessage")
    val errorMessage: String,

    @SerialName("Regex")
    val regex: String,

    @SerialName("HideIfValid")
    val hideIfValid: Boolean
)

internal fun PasswordPoliciesResponse.toPasswordPolicies(): List<PasswordPolicy> =
    passwordPolicies.map { it.toPasswordPolicy() }

internal fun PasswordPolicyResource.toPasswordPolicy(): PasswordPolicy {
    val regex = try {
        Regex(regex)
    } catch (e: PatternSyntaxException) {
        CoreLogger.e(LogTag.INVALID_PASS_POLICY_REGEX, e, "Invalid regex for password policy ${name}.")
        null
    }
    return PasswordPolicy(
        name = name,
        state = when {
            regex == null -> IntEnum(state, PasswordPolicyState.InvalidRegex)
            else -> IntEnum(state, state.toPasswordPolicyState())
        },
        requirementMessage = requirementMessage,
        errorMessage = errorMessage,
        regex = regex,
        hideIfValid = hideIfValid
    )
}

internal fun Int.toPasswordPolicyState() = when (this) {
    0 -> PasswordPolicyState.Disabled
    1 -> PasswordPolicyState.Enabled
    2 -> PasswordPolicyState.Optional
    else -> null
}
