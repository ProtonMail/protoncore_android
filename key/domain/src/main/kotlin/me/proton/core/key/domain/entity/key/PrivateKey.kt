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

package me.proton.core.key.domain.entity.key

import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.keystore.EncryptedByteArray

data class PrivateKey(
    val key: Armored,
    val isPrimary: Boolean,
    val isActive: Boolean = true,
    val canEncrypt: Boolean = true,
    val canVerify: Boolean = true,
    val passphrase: EncryptedByteArray?
) {
    /**
     * True if no passphrase is associated, thereby only public crypto functions are available.
     *
     * False if a passphrase is associated, thereby public and private crypto functions are available.
     */
    @Deprecated("Please use isUnlockable instead.", ReplaceWith("isUnlockable.not()"))
    val isLocked = passphrase == null

    /**
     * False if no passphrase is associated, thereby only public crypto functions are available.
     *
     * True if a passphrase is associated, thereby public and private crypto functions are available.
     */
    val isUnlockable = passphrase != null
}
