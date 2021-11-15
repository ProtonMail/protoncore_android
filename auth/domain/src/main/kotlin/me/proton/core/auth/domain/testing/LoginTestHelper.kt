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

package me.proton.core.auth.domain.testing

import androidx.annotation.RestrictTo
import kotlinx.coroutines.runBlocking
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.CreateLoginSession
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import javax.inject.Inject

@RestrictTo(RestrictTo.Scope.TESTS)
class LoginTestHelper @Inject constructor(
    private val accountManager: AccountManager,
    private val postLoginAccountSetup: PostLoginAccountSetup,
    private val accountType: AccountType,
    private val createLoginSession: CreateLoginSession,
    private val keyStoreCrypto: KeyStoreCrypto
) {
    /** Logs in the given user.
     * This function blocks current thread.
     */
    fun login(username: String, password: String): SessionInfo = runBlocking {
        val encryptedPassword = password.encrypt(keyStoreCrypto)
        val sessionInfo = createLoginSession(username, encryptedPassword, accountType)
        val result = postLoginAccountSetup(sessionInfo, encryptedPassword, accountType)
        check(result is PostLoginAccountSetup.Result.UserUnlocked) {
            "Unexpected login result: $result"
        }
        sessionInfo
    }

    /** Clears the given login session.
     * The user is logged out (remote server call) and the session is removed from local database.
     * This function blocks current thread.
     */
    fun logout(sessionInfo: SessionInfo) = runBlocking {
        accountManager.removeAccount(sessionInfo.userId)
    }
}
