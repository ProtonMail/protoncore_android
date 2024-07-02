/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.usersettings.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

/**
 * Returns a list of registered FIDO2 security keys for the given user.
 */
class ObserveRegisteredSecurityKeys @Inject constructor(
    private val accountRepository: AccountRepository,
    private val isFido2Enabled: IsFido2Enabled,
    private val observeUserSettings: ObserveUserSettings
) {
    operator fun invoke(
        userId: UserId,
        refresh: Boolean = false
    ): Flow<List<Fido2RegisteredKey>> {
        if (!isFido2Enabled(userId)) return flowOf(emptyList())
        return observeUserSettings(userId, refresh).map { it?.twoFA?.registeredKeys ?: emptyList() }
    }

    suspend operator fun invoke(
        refresh: Boolean = false
    ): Flow<List<Fido2RegisteredKey>> =
        accountRepository.getPrimaryUserId().first()?.let { userId ->
            invoke(userId, refresh)
        } ?: run {
            flowOf(emptyList())
        }
}
