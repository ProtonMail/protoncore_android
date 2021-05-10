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

package me.proton.android.core.coreexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.mailmessage.domain.usecase.SendEmailDirect
import me.proton.core.user.domain.UserManager
import me.proton.core.util.kotlin.exhaustive
import timber.log.Timber
import java.io.ByteArrayInputStream
import javax.inject.Inject

@HiltViewModel
class MailMessageViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val sendEmailDirect: SendEmailDirect
) : ViewModel() {

    private val action = MutableSharedFlow<Action>()

    sealed class Action {
        object SendDirect : Action()
    }

    sealed class State {
        object Processing : State()
        object Success : State()
        sealed class Error : State() {
            data class Message(val message: String?) : Error()
            data class GettingPublicAddressKeys(val emailAddresses: List<String>) : Error()
            data class GeneratingEmailPackages(val emailAddresses: List<String>) : Error()
            data class EncryptingAttachments(val attachmentFileNames: List<String>) : Error()
            object GettingSenderAddress : Error()
        }
    }

    private fun getSendDirectState() = flow {
        emit(State.Processing)

        val userId = accountManager.getPrimaryUserId().first()!!
        val userAddress = userManager.getAddresses(userId).first()

        val array1 = "Plain Attachment text 1".toByteArray()
        val array2 = "Plain Attachment text 2".toByteArray()
        val attachments = listOf(
            SendEmailDirect.Arguments.Attachment(
                fileName = "att1.txt",
                fileSize = array1.size,
                mimeType = "text/plain",
                inputStream = ByteArrayInputStream(array1)
            ),
            SendEmailDirect.Arguments.Attachment(
                fileName = "att2.txt",
                fileSize = array2.size,
                mimeType = "text/plain",
                inputStream = ByteArrayInputStream(array2)
            )
        )
        val arguments = SendEmailDirect.Arguments(
            "Test email",
            "Test body of an email",
            "text/plain",
            listOf(/*"adamtst@pm.me", "adamprotonmail@gmail.com"*/),
            attachments
        )
        when (val result = sendEmailDirect.invoke(userAddress, arguments)) {
            is SendEmailDirect.Result.Error.EncryptingAttachments -> State.Error.EncryptingAttachments(result.attachmentFileNames)
            is SendEmailDirect.Result.Error.GeneratingEmailPackages -> State.Error.GeneratingEmailPackages(result.emailAddresses)
            is SendEmailDirect.Result.Error.GettingPublicAddressKeys -> State.Error.GettingPublicAddressKeys(result.emailAddresses)
            is SendEmailDirect.Result.Error.GettingSenderAddress -> State.Error.GettingSenderAddress
            is SendEmailDirect.Result.Success -> State.Success
        }.let {
            emit(it)
        }
    }.catch { error ->
        Timber.e(error)
        emit(State.Error.Message(error.message))
    }

    fun sendDirect() = viewModelScope.launch { action.emit(Action.SendDirect) }

    fun getState() = action.flatMapLatest { action ->
        when (action) {
            is Action.SendDirect -> getSendDirectState()
        }.exhaustive
    }
}
