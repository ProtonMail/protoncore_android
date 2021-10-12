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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.utils.createToBeEncryptedAndSignedVCard
import me.proton.android.core.coreexample.utils.createToBeSignedVCard
import me.proton.android.core.coreexample.utils.prettyPrint
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.contact.domain.decryptContactCard
import me.proton.core.contact.domain.encryptAndSignContactCard
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.contact.domain.signContactCard
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.arch.DataResult
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accountManager: AccountManager,
    private val contactRepository: ContactRepository,
    private val userManager: UserManager,
    private val cryptoContext: CryptoContext,
) : ViewModel() {

    private val mutableViewState = MutableStateFlow<ViewState?>(null)
    val viewState = mutableViewState.asStateFlow().filterNotNull()

    private var contact: Contact? = null
    private val contactId: ContactId = ContactId(savedStateHandle.get(ARG_CONTACT_ID)!!)
    private var observeContactJob: Job? = null

    init {
        CoreLogger.d("contact", "presenting contact $contactId")
        observeContact()
    }

    private fun observeContact() {
        cancelObserveContact()
        observeContactJob = viewModelScope.launch {
            accountManager.getPrimaryUserId().filterNotNull()
                .flatMapLatest { userId ->
                    val user = userManager.getUser(userId)
                    contactRepository.observeContactWithCards(userId, contactId).map { dataResult ->
                        handleResult(user, dataResult)
                    }
                }.collect()
        }
    }

    private fun cancelObserveContact() {
        observeContactJob?.cancel()
        observeContactJob = null
    }

    private fun handleResult(user: User, result: DataResult<ContactWithCards>) {
        mutableViewState.value = when (result) {
            is DataResult.Processing -> ViewState.Processing
            is DataResult.Error -> handleDataResultError(result)
            is DataResult.Success -> handleSuccess(user, result.value)
        }.exhaustive
    }

    private fun handleSuccess(
        user: User,
        contact: ContactWithCards
    ): ViewState.Success {
        this.contact = contact.contact
        val decryptedCards = user.useKeys(cryptoContext) {
            contact.contactCards.map { decryptContactCard(it) }
        }
        return ViewState.Success(
            rawContact = contact.prettyPrint(),
            vCardContact = decryptedCards.prettyPrint()
        )
    }

    private fun handleDataResultError(error: DataResult.Error): ViewState.Error {
        val errorMessage = error.message ?: "Unknown error"
        val errorCause = error.cause ?: Throwable(errorMessage)
        return handleError(errorCause, errorMessage)
    }

    private fun handleError(throwable: Throwable, errorMessage: String): ViewState.Error {
        CoreLogger.e("contact", throwable, errorMessage)
        return ViewState.Error(errorMessage)
    }

    fun deleteContact() {
        mutableViewState.value = ViewState.Processing
        viewModelScope.launch(Dispatchers.Default) {
            try {
                cancelObserveContact()
                val userId = accountManager.getPrimaryUserId().filterNotNull().first()
                contactRepository.deleteContacts(userId, listOf(contactId))
                mutableViewState.value = ViewState.Deleted
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                handleError(throwable, "Error deleting contact $contactId")
            }
        }
    }

    fun updateContact() {
        viewModelScope.launch(Dispatchers.Default) {
            mutableViewState.value = ViewState.Processing
            try {
                val contactName = requireNotNull(contact).name
                val userId = accountManager.getPrimaryUserId().filterNotNull().first()
                val user = userManager.getUser(userId)
                val cards = user.useKeys(cryptoContext) {
                    listOf(
                        signContactCard(createToBeSignedVCard(contactName)),
                        encryptAndSignContactCard(createToBeEncryptedAndSignedVCard(contactName))
                    )
                }
                contactRepository.updateContact(userId, contactId, cards)
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                handleError(throwable, "Error updating contact $contactId")
            }
        }
    }

    sealed class ViewState {
        object Processing : ViewState()
        data class Error(val reason: String) : ViewState()
        data class Success(val rawContact: String, val vCardContact: String) : ViewState()
        object Deleted : ViewState()
    }

    companion object {
        const val ARG_CONTACT_ID = "arg.contactId"
    }
}
