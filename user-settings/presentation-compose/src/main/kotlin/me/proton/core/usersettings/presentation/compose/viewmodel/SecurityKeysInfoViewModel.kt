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

package me.proton.core.usersettings.presentation.compose.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.fido.domain.entity.Fido2RegisteredKey
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.usersettings.domain.usecase.ObserveRegisteredSecurityKeys
import javax.inject.Inject

@HiltViewModel
class SecurityKeysInfoViewModel @Inject constructor(
    accountManager: AccountManager,
    private val observeRegisteredSecurityKeys: ObserveRegisteredSecurityKeys
) : ProtonViewModel() {

    val state: StateFlow<SecurityKeysState> =
        accountManager.getPrimaryUserId()
            .filterNotNull()
            .flatMapLatest { userId -> observeState(userId) }
            .catch {
                emit(SecurityKeysState.Error(it))
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis),
                SecurityKeysState.Loading
            )

    private fun observeState(userId: UserId): Flow<SecurityKeysState> =
        observeRegisteredSecurityKeys(userId).mapLatest {
            SecurityKeysState.Success(it)
        }
}

sealed class SecurityKeysState {
    data object Loading : SecurityKeysState()

    data class Success(val keys: List<Fido2RegisteredKey>) : SecurityKeysState()

    data class Error(val throwable: Throwable?) : SecurityKeysState()
}
