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

package me.proton.android.core.coreexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.utils.createToBeEncryptedAndSignedVCard
import me.proton.android.core.coreexample.utils.createToBeSignedVCard
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.contact.domain.encryptAndSignContactCard
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.contact.domain.signContactCard
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.UserManager
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@HiltViewModel
class CreateContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val userManager: UserManager,
    private val accountManager: AccountManager,
    private val cryptoContext: CryptoContext,
) : ViewModel() {

    private val mutableState = MutableStateFlow<State>(State.Idling)
    val state = mutableState.asStateFlow()

    fun createContact(name: String) {
        mutableState.value = State.Processing
        viewModelScope.launch {
            try {
                val userId = accountManager.getPrimaryUserId().filterNotNull().first()
                val cards = userManager.getUser(userId).useKeys(cryptoContext) {
                    listOf(
                        signContactCard(createToBeSignedVCard(name)),
                        encryptAndSignContactCard(createToBeEncryptedAndSignedVCard(name))
                    )
                }
                contactRepository.createContact(userId, cards)
                mutableState.value = State.Success
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                CoreLogger.e("contact", throwable)
                mutableState.value = State.Error(throwable.message ?: "Unknown error")
            }
            mutableState.value = State.Idling
        }
    }

    sealed class State {
        object Idling : State()
        object Processing : State()
        data class Error(val reason: String) : State()
        object Success : State()
    }
}
