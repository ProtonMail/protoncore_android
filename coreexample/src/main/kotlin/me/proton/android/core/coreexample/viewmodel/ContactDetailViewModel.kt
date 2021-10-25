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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.proton.android.core.coreexample.utils.createToBeEncryptedAndSignedVCard
import me.proton.android.core.coreexample.utils.createToBeSignedVCard
import me.proton.android.core.coreexample.utils.prettyPrint
import me.proton.core.contact.domain.decryptContactCard
import me.proton.core.contact.domain.encryptAndSignContactCard
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.contact.domain.entity.ContactWithCards
import me.proton.core.contact.domain.repository.ContactRepository
import me.proton.core.contact.domain.signContactCard
import me.proton.core.contact.domain.usecase.UpdateContactRemote
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.UserManager
import me.proton.core.util.kotlin.truncateToLength
import javax.inject.Inject

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contactRepository: ContactRepository,
    private val updateContactRemote: UpdateContactRemote,
    private val userManager: UserManager,
    private val cryptoContext: CryptoContext,
) : ActionViewModel<ContactDetailViewModel.Action, ContactDetailViewModel.State>() {

    private val userId = UserId(requireNotNull(savedStateHandle.get(ARG_USER_ID)))
    private val contactId = ContactId(requireNotNull(savedStateHandle.get(ARG_CONTACT_ID)))

    sealed class Action {
        object Observe : Action()
        object Update : Action()
        object Delete : Action()
    }

    sealed class State {
        object Loading : State()
        data class Success(val rawContact: String, val vCardContact: String) : State()
        data class Error(val error: String?) : State()
        object Updated : State()
        object Deleted : State()
    }

    init {
        dispatch(Action.Observe)
    }

    override fun process(action: Action): Flow<State> = when (action) {
        Action.Observe -> observeContact()
        Action.Delete -> deleteContact()
        Action.Update -> updateContact()
    }

    private fun observeContact(): Flow<State> = flow {
        emit(State.Loading)
        emitAll(contactRepository.observeContactWithCards(userId, contactId).mapState())
    }

    private fun deleteContact(): Flow<State> = flow {
        emit(State.Loading)
        contactRepository.deleteContacts(userId, listOf(contactId))
        emit(State.Deleted)
    }.catch {
        emit(State.Error(it.message))
    }

    private fun updateContact(): Flow<State> = flow {
        emit(State.Loading)
        val user = userManager.getUser(userId)
        val contactName = requireNotNull(contactRepository.getContactWithCards(userId, contactId)).contact.name
        val cards = user.useKeys(cryptoContext) {
            listOf(
                signContactCard(createToBeSignedVCard(contactName)),
                encryptAndSignContactCard(createToBeEncryptedAndSignedVCard(contactName))
            )
        }
        updateContactRemote(userId, contactId, cards)
        emit(State.Updated)
        emitAll(observeContact())
    }.catch {
        emit(State.Error(it.message))
    }

    private fun Flow<DataResult<ContactWithCards>>.mapState(): Flow<State> = map {
        when (it) {
            is DataResult.Error -> State.Error(it.message)
            is DataResult.Processing -> State.Loading
            is DataResult.Success -> {
                val user = userManager.getUser(userId)
                val decryptedCards = user.useKeys(cryptoContext) {
                    it.value.contactCards.map { card -> decryptContactCard(card) }
                }
                State.Success(
                    rawContact = it.value.prettyPrint().truncateToLength(10000).toString(),
                    vCardContact = decryptedCards.prettyPrint()
                )
            }
        }
    }

    companion object {
        const val ARG_USER_ID = "arg.userId"
        const val ARG_CONTACT_ID = "arg.contactId"
    }
}
