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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.contact.domain.entity.ContactEmail
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.util.kotlin.Logger
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val accountManager: AccountManager,
    private val logger: Logger,
) : ViewModel() {

    private val _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow().filterNotNull()

    init {
        viewModelScope.launch { observeFirstAccountContactsEmails() }
    }

    private suspend fun observeFirstAccountContactsEmails() {
        accountManager.getAccounts().collect { accounts ->
            accounts.firstOrNull()?.let { account ->
                contactRepository.getContactEmails(account.userId, refresh = true).collect { result ->
                    when (result) {
                        is DataResult.Error.Local -> _state.value = State.Error(result.message)
                        is DataResult.Error.Remote -> _state.value = State.Error(result.message)
                        is DataResult.Processing -> _state.value = State.Processing
                        is DataResult.Success -> {
                            result.value.firstOrNull()?.let { email ->
                                viewModelScope.launch { testContactApi(account.userId, email.contactId) }
                            }
                            _state.value = State.Contacts(result.value)
                        }
                    }.exhaustive
                }
            }
        }
    }

    private suspend fun testContactApi(userId: UserId, contactId: String) {
        val contact = contactRepository.getContact(sessionUserId = userId, contactId = contactId, refresh = true)
        logger.d("contact", contact.toString())
    }

    sealed class State {
        object Processing : State()
        data class Contacts(val emails: List<ContactEmail>) : State()
        data class Error(val reason: String?) : State()
    }
}