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

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.key.domain.unlock
import me.proton.core.key.domain.unlockOrNull
import java.io.Closeable

data class PrivateKeyRing(
    val context: CryptoContext,
    val keys: List<PrivateKey>
) : Closeable {

    private val primaryKey by lazy {
        keys.firstOrNull { it.isPrimary } ?: throw CryptoException("No primary key available.")
    }

    private val unlockedPrimaryKeyDelegate = lazy { primaryKey.unlock(context) }
    private val unlockedKeysDelegate = lazy { keys.mapNotNull { it.unlockOrNull(context) } }

    val unlockedPrimaryKey by unlockedPrimaryKeyDelegate
    val unlockedKeys by unlockedKeysDelegate

    override fun close() {
        if (unlockedPrimaryKeyDelegate.isInitialized()) unlockedPrimaryKey.unlockedKey.close()
        if (unlockedKeysDelegate.isInitialized()) unlockedKeys.forEach { it.unlockedKey.close() }
    }
}
