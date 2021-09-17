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
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val accountManager: AccountManager,
) : ViewModel() {

    private val mutableState = MutableStateFlow<State?>(null)
    val state = mutableState.asStateFlow().filterNotNull()

    init {
        viewModelScope.launch { observePrimaryAccountContacts() }
    }

    private suspend fun observePrimaryAccountContacts() {
        accountManager.getPrimaryUserId().filterNotNull().collect { userId ->
            contactRepository.observeAllContacts(userId).collect { result ->
                when (result) {
                    is DataResult.Error.Local -> handleDataResultError(result)
                    is DataResult.Error.Remote -> handleDataResultError(result)
                    is DataResult.Processing -> mutableState.value = State.Processing
                    is DataResult.Success -> mutableState.value = State.Contacts(result.value)
                }.exhaustive
            }
        }
    }

    private fun handleDataResultError(error: DataResult.Error) {
        val errorMessage = error.message ?: "Unknown error"
        val errorCause = error.cause ?: Throwable(errorMessage)
        CoreLogger.e("contact", errorCause, errorMessage)
        mutableState.value = State.Error(errorMessage)
    }

    sealed class State {
        object Processing : State()
        data class Contacts(val contacts: List<Contact>) : State()
        data class Error(val reason: String) : State()
    }
}
