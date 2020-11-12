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

package me.proton.core.auth.presentation.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.domain.usecase.AvailableDomains
import me.proton.core.auth.domain.usecase.UsernameAvailability
import me.proton.core.presentation.viewmodel.ProtonViewModel
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View model responsible for fetching the available domains and checking the entered username availability.
 * @author Dino Kadrikj.
 */
class CreateAddressViewModel @ViewModelInject constructor(
    private val usernameAvailability: UsernameAvailability,
    private val availableDomains: AvailableDomains
) : ProtonViewModel(), ViewStateStoreScope {

    val state = ViewStateStore<UsernameAvailability.State>().lock
    val domainsState = ViewStateStore<AvailableDomains.State>().lock

    var domain: String? = null

    fun getAvailableDomains() {
        availableDomains()
            .onEach {
                if (it is AvailableDomains.State.Success) {
                    domain = it.firstDomainOrDefault
                }
                domainsState.post(it)
            }
            .launchIn(viewModelScope)
    }

    fun checkUsernameAvailability(
        username: String
    ) {
        usernameAvailability(username)
            .onEach {
                if (it is UsernameAvailability.State.Success) {
                    state.post(it.copy(domain = domain))
                } else {
                    state.post(it)
                }
            }
            .launchIn(viewModelScope)
    }
}
