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

package me.proton.core.crypto.android.context

import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.android.simple.KeyStoreSimpleCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.simple.SimpleCrypto

/**
 * [CryptoContext] for Android platform.
 *
 * @see KeyStoreSimpleCrypto
 * @see GOpenPGPCrypto
 */
class AndroidCryptoContext(
    override val simpleCrypto: SimpleCrypto = KeyStoreSimpleCrypto.default,
    override val pgpCrypto: PGPCrypto = GOpenPGPCrypto()
) : CryptoContext
