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

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.passvalidator.domain.entity.PasswordValidatorResult
import me.proton.core.passvalidator.domain.usecase.ValidatePassword
import javax.inject.Inject

@HiltViewModel
public open class PasswordPolicyReportViewModel @Inject constructor(
    private val validatePassword: ValidatePassword
) : BaseViewModel<PasswordPolicyReportAction, PasswordPolicyReportState>(
    initialAction = PasswordPolicyReportAction.NoOp,
    initialState = PasswordPolicyReportState.Loading
) {
    override fun onAction(action: PasswordPolicyReportAction): Flow<PasswordPolicyReportState> = when (action) {
        is PasswordPolicyReportAction.NoOp -> emptyFlow()
        is PasswordPolicyReportAction.Validate -> onValidate(action.password, action.userId)
    }

    override suspend fun FlowCollector<PasswordPolicyReportState>.onError(throwable: Throwable) {
        emit(PasswordPolicyReportState.Hidden(token = null))
    }

    private fun onValidate(
        password: String,
        userId: UserId?
    ): Flow<PasswordPolicyReportState> = validatePassword(password, userId).map { (validationResults, token) ->
        when {
            validationResults.isEmpty() -> PasswordPolicyReportState.Hidden(token = token)
            else -> {
                val hasSingleRequirement = validationResults.hasSingleRequirement()
                PasswordPolicyReportState.Idle(
                    messages = validationResults.mapNotNull {
                        it.toPasswordPolicyReportMessage(hasSingleRequirement)
                    },
                    token = token
                )
            }
        }
    }
}

private fun List<PasswordValidatorResult>.hasSingleRequirement(): Boolean =
    filter { !it.hideIfValid }.size == 1

private fun PasswordValidatorResult.toPasswordPolicyReportMessage(
    hasSingleRequirement: Boolean
): PasswordPolicyReportMessage? = when (isValid) {
    true -> when {
        hideIfValid -> null
        hasSingleRequirement -> PasswordPolicyReportMessage.Hint(
            message = errorMessage,
            success = true
        )

        else -> PasswordPolicyReportMessage.Requirement(
            message = requirementMessage,
            success = true
        )
    }

    false -> {
        when {
            hideIfValid -> PasswordPolicyReportMessage.Error(message = errorMessage)
            hasSingleRequirement -> PasswordPolicyReportMessage.Hint(
                message = errorMessage,
                success = false
            )

            else -> PasswordPolicyReportMessage.Requirement(
                message = requirementMessage,
                success = false
            )
        }
    }

    null -> null
}
