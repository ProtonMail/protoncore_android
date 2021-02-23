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

package me.proton.core.user.domain

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.NestedPrivateKey
import me.proton.core.key.domain.generateNestedPrivateKey
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.emailSplit

/**
 * Generate and encrypt a new [NestedPrivateKey] from [UserAddress] keys.
 *
 * Note: Only this [UserAddress] will be able to decrypt.
 */
fun UserAddress.generateNestedPrivateKey(cryptoContext: CryptoContext): NestedPrivateKey = useKeys(cryptoContext) {
    emailSplit.let { generateNestedPrivateKey(it.username, it.domain) }
}
