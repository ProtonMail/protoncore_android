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
import me.proton.core.auth.domain.usecase.UpdateExternalAccount
import me.proton.core.auth.domain.usecase.UpdateUsernameOnlyAccount
import me.proton.core.auth.domain.usecase.onSuccess
import me.proton.core.auth.presentation.entity.AddressesResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View model responsible for executing the business logic for address creation and
 * @author Dino Kadrikj.
 */
class CreateAddressResultViewModel @ViewModelInject constructor(
    private val updateExternalAccount: UpdateExternalAccount,
    private val updateUsernameOnlyAccount: UpdateUsernameOnlyAccount,
    private val availableDomains: AvailableDomains
) : ProtonViewModel(), ViewStateStoreScope {

    val externalAccountUpgradeState = ViewStateStore<UpdateExternalAccount.State>().lock
    val usernameOnlyAccountUpgradeState = ViewStateStore<UpdateUsernameOnlyAccount.State>().lock
    val domainsState = ViewStateStore<AvailableDomains.State>().lock

    lateinit var domain: String

    init {
        getAvailableDomains()
    }

    private fun getAvailableDomains() {
        availableDomains()
            .onSuccess { domain = it.firstOrDefault }
            .onEach { domainsState.post(it) }
            .launchIn(viewModelScope)
    }

    fun upgradeAccount(
        addresses: AddressesResult?,
        sessionId: SessionId,
        username: String,
        domain: String? = null,
        passphrase: ByteArray
    ) {
        addresses?.let {
            if (!it.usernameOnly && it.allExternal) {
                upgradeExternalAccount(
                    sessionId,
                    username,
                    domain ?: this@CreateAddressResultViewModel.domain,
                    passphrase
                )
            } else {
                upgradeUsernameOnlyAccount(sessionId, username, this@CreateAddressResultViewModel.domain, passphrase)
            }
        }
    }

    private fun upgradeExternalAccount(sessionId: SessionId, username: String, domain: String, passphrase: ByteArray) {
        updateExternalAccount(sessionId, username, domain, passphrase)
            .onEach {
                externalAccountUpgradeState.post(it)
            }
            .launchIn(viewModelScope)
    }

    private fun upgradeUsernameOnlyAccount(
        sessionId: SessionId,
        username: String,
        domain: String,
        passphrase: ByteArray
    ) {
        updateUsernameOnlyAccount(sessionId, domain, username, passphrase)
            .onEach {
                usernameOnlyAccountUpgradeState.post(it)
            }
            .launchIn(viewModelScope)
    }
}
