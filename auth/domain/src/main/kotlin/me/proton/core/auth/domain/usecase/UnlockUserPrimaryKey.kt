/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.auth.domain.usecase

import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.extension.hasKeys
import me.proton.core.util.kotlin.coroutine.result
import javax.inject.Inject

/**
 * Try to unlock the primary [UserKey] with the given password.
 *
 * - With keys: on UnlockResult.Success, the passphrase is stored and the User keys ready to be used.
 * - Without keys: this function always return UnlockResult.Success.
 * - For VPN: this function always return UnlockResult.Success.
 */
class UnlockUserPrimaryKey @Inject constructor(
    private val userManager: UserManager,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val product: Product
) {
    /**
     * Try to unlock the user with the given password.
     */
    @Deprecated(
        message = "This intermediate use case will be removed.",
        replaceWith = ReplaceWith(
            expression = "UserManager.unlockWithPassword(userId, password)",
            imports = ["me.proton.core.user.domain.UserManager"]
        )
    )
    suspend operator fun invoke(
        userId: UserId,
        password: EncryptedString
    ): UserManager.UnlockResult {
        return when {
            product == Product.Vpn -> UserManager.UnlockResult.Success
            !userManager.getUser(userId).hasKeys() -> UserManager.UnlockResult.Success
            else -> password.decrypt(keyStoreCrypto).toByteArray().use {
                userManager.unlockWithPassword(userId, it)
            }
        }.let { unlockResult ->
            result("unlockUserPrimaryKey") { unlockResult }
        }
    }
}
