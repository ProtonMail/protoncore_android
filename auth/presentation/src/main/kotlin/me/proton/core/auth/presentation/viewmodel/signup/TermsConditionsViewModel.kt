/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.auth.presentation.viewmodel.signup

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.presentation.viewmodel.ProtonViewModel

internal class TermsConditionsViewModel @ViewModelInject constructor(
    private val networkManager: NetworkManager
) : ProtonViewModel() {

    private val _networkConnectionState = MutableStateFlow<Boolean?>(null)

    val networkConnectionState = _networkConnectionState.asStateFlow()

    /**
     * Watches for any network changes and informs the UI for any state change so that it can act
     * accordingly for any network dependent tasks.
     */
    fun watchNetwork() {
        viewModelScope.launch {
            networkManager.observe().collect { status ->
                _networkConnectionState.tryEmit(
                    when (status) {
                        NetworkStatus.Metered, NetworkStatus.Unmetered -> true
                        else -> false
                    }
                )
            }
        }
    }
}
