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

package me.proton.core.passvalidator.data.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.passvalidator.data.entity.PasswordPolicyState
import me.proton.core.passvalidator.data.feature.IsPasswordPolicyEnabled
import me.proton.core.passvalidator.data.repository.PasswordPolicyRepository
import me.proton.core.passvalidator.data.validator.PasswordPolicyValidator
import me.proton.core.passvalidator.data.validator.PasswordValidator
import javax.inject.Inject

internal class ObservePasswordPolicyValidators @Inject internal constructor(
    private val isPasswordPolicyEnabled: IsPasswordPolicyEnabled,
    private val passwordPolicyRepository: PasswordPolicyRepository
) {
    operator fun invoke(
        userId: UserId?
    ): Flow<List<PasswordValidator>> = when {
        !isPasswordPolicyEnabled(userId) -> flowOf(emptyList())
        else -> passwordPolicyRepository.observePasswordPolicies(userId).map { policies ->
            policies.filter { it.state.enum == PasswordPolicyState.Enabled }
        }.map { policies ->
            policies.map { PasswordPolicyValidator(it) }
        }
    }
}
