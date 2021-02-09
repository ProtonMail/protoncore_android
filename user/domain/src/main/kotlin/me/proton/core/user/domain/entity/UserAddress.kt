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

package me.proton.core.user.domain.entity

import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.extension.areAllLocked
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.UserManager

data class AddressId(val id: String)

data class UserAddress(
    val userId: UserId,
    val addressId: AddressId,
    val email: String,
    val displayName: String? = null,
    val domainId: String? = null,
    val canSend: Boolean,
    val canReceive: Boolean,
    val enabled: Boolean,
    val type: AddressType? = null,
    val order: Int,
    /**
     * Address Private Keys used by crypto functions (e.g. encrypt, decrypt, sign, verify).
     *
     * Example:
     * ```
     * userAddress.useKeys(context) {
     *     val text = "text"
     *
     *     val encryptedText = encryptText(text)
     *     val signedText = signText(text)
     *
     *     val decryptedText = decryptText(encryptedText)
     *     val isVerified = verifyText(decryptedText, signedText)
     * }
     * ```
     * @see [useKeys]
     * @see [areAllLocked]
     * @see [UserManager.unlockWithPassword]
     * @see [UserManager.unlockWithPassphrase]
     * @see [UserManager.lock]
     * */
    override val keys: List<UserAddressKey>
) : KeyHolder

enum class AddressType(val value: Int) {
    /** First address the user created using a ProtonMail domain. */
    Original(1),

    /** Subsequent addresses created using a ProtonMail domain. */
    Alias(2),

    /** Custom domain address. */
    Custom(3),

    /** Premium "pm.me" domain. */
    Premium(4),

    /** External address. */
    External(5);

    companion object {
        val map = values().associateBy { it.value }
    }
}
