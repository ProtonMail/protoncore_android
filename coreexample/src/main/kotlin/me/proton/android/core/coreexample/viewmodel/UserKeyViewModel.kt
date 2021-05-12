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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transformLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.arch.DataResult
import me.proton.core.key.domain.decryptTextOrNull
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.extension.areAllLocked
import me.proton.core.key.domain.signText
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyText
import me.proton.core.user.domain.UserManager
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserKeyViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val cryptoContext: CryptoContext
) : ViewModel() {

    sealed class UserKeyState {
        object Success : UserKeyState()
        sealed class Error : UserKeyState() {
            data class Message(val message: String?) : Error()
            object NoPrimaryAccount : Error()
            object KeyLocked : Error()
            object CannotDecrypt : Error()
            object CannotVerify : Error()
        }
    }

    fun getUserKeyState() = accountManager.getPrimaryAccount()
        .flatMapLatest { primary -> primary?.let { userManager.getUserFlow(it.userId) } ?: flowOf(null) }
        .transformLatest { result ->
            if (result == null || result !is DataResult.Success || result.value == null) {
                emit(UserKeyState.Error.NoPrimaryAccount)
                return@transformLatest
            }
            val user = result.value!!
            if (user.keys.areAllLocked()) {
                emit(UserKeyState.Error.KeyLocked)
                return@transformLatest
            }
            val state = user.useKeys(cryptoContext) {
                val message = "message"
                val encrypted = encryptText(message)
                val signature = signText(message)
                val decrypted = decryptTextOrNull(encrypted) ?: return@useKeys UserKeyState.Error.CannotDecrypt
                if (verifyText(decrypted, signature))
                    return@useKeys UserKeyState.Success
                else
                    return@useKeys UserKeyState.Error.CannotVerify
            }
            emit(state)
        }.catch { error ->
            Timber.e(error)
            emit(UserKeyState.Error.Message(error.message))
        }
}
