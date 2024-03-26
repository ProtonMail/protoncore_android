/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.userrecovery.data

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.user.domain.UserManager
import me.proton.core.userrecovery.domain.LogTag
import me.proton.core.userrecovery.domain.usecase.GetRecoveryFile
import me.proton.core.userrecovery.domain.usecase.GetRecoveryInactivePrivateKeys
import me.proton.core.userrecovery.domain.usecase.GetRecoveryPrivateKeys
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRecoveryHandler @Inject constructor(
    internal val scopeProvider: CoroutineScopeProvider,
    internal val cryptoContext: CryptoContext,
    internal val accountManager: AccountManager,
    internal val userManager: UserManager,
    internal val getRecoveryFile: GetRecoveryFile,
    internal val getRecoveryPrivateKeys: GetRecoveryPrivateKeys,
    internal val getRecoveryInactivePrivateKeys: GetRecoveryInactivePrivateKeys,
) {
    fun start() {
        scopeProvider.GlobalDefaultSupervisedScope.launch {
            val userId = accountManager.getPrimaryUserId().firstOrNull() ?: return@launch

            val message = getRecoveryFile(userId)
            CoreLogger.d(LogTag.DEFAULT, "Recovery file: $message")
            val keys = getRecoveryPrivateKeys(userId, message)
            CoreLogger.d(LogTag.DEFAULT, "Recovery Private Keys: $keys")
            val recoverable = getRecoveryInactivePrivateKeys(userId, keys)
            CoreLogger.d(LogTag.DEFAULT, "Recovery inactive keys: $recoverable")
        }
    }
}
