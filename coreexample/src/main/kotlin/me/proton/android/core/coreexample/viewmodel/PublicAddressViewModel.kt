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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.arch.DataResult
import me.proton.core.key.domain.decryptTextOrNull
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.extension.areAllLocked
import me.proton.core.key.domain.repository.PublicAddressKeyRepository
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.primary

class PublicAddressViewModel @ViewModelInject constructor(
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val publicAddressKeyRepository: PublicAddressKeyRepository,
    private val cryptoContext: CryptoContext
) : ViewModel() {

    sealed class PublicAddressState {
        object Unknown : PublicAddressState()
        object Success : PublicAddressState()
        sealed class Error : PublicAddressState() {
            object NoPrimaryAddress : Error()
            object NoPublicAddress : Error()
            object KeyLocked : Error()
            object CannotDecrypt : Error()
            object DecryptedAreNotEqual : Error()
        }
    }

    fun getPublicAddressState() = accountManager
        .getPrimaryAccount()
        .flatMapLatest { primary -> primary?.let { userManager.getUser(it.userId) } ?: flowOf(null) }
        .transformLatest { result ->
            if (result == null || result !is DataResult.Success || result.value == null) {
                emit(PublicAddressState.Unknown)
                return@transformLatest
            }
            val user = result.value!!
            if (user.keys.areAllLocked()) {
                emit(PublicAddressState.Error.KeyLocked)
                return@transformLatest
            }

            // Get Addresses from primary User.
            val addresses = userManager.getAddresses(user.userId, refresh = true)
                .mapLatest { it as? DataResult.Success }
                .mapLatest { it?.value }
                .filterNotNull()
                .firstOrNull()

            // Get Primary address from primary User.
            val primaryAddress = addresses?.primary()
            if (primaryAddress == null) {
                emit(PublicAddressState.Error.NoPrimaryAddress)
                return@transformLatest
            }

            // Get PublicAddress from server.
            val publicAddress = publicAddressKeyRepository.getPublicAddress(user.userId, primaryAddress.email)
            if (publicAddress == null) {
                emit(PublicAddressState.Error.NoPublicAddress)
                return@transformLatest
            }

            // Simulate a third party User encryption for the current primary User.
            val message = "message"
            val encryptedText = publicAddress.encryptText(cryptoContext, message)

            // Try to decrypt.
            val state = primaryAddress.useKeys(cryptoContext) {
                val decryptedText = decryptTextOrNull(encryptedText)
                    ?: return@useKeys PublicAddressState.Error.CannotDecrypt

                if (decryptedText != message)
                    return@useKeys PublicAddressState.Error.DecryptedAreNotEqual
                else
                    return@useKeys PublicAddressState.Success
            }
            emit(state)
        }
}
