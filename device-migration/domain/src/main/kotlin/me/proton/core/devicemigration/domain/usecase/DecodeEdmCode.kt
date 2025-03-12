/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.domain.usecase

import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.devicemigration.domain.entity.ChildClientId
import me.proton.core.devicemigration.domain.entity.EdmParams
import me.proton.core.devicemigration.domain.entity.EncryptionKey
import me.proton.core.devicemigration.domain.entity.UserCode
import me.proton.core.util.kotlin.takeIfNotBlank
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

public class DecodeEdmCode @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto
) {
    /**
     * Format: UserCode:b64(EncryptionKey):ChildClientID
     */
    @OptIn(ExperimentalEncodingApi::class)
    public operator fun invoke(encoded: String): EdmParams? {
        val tokens = encoded.split(":")
        val userCode = tokens.getNotBlankOrNull(0)?.let { UserCode(it) }
        val encryptionKey = tokens.getNotBlankOrNull(1)
            ?.let { Base64.decodeOrNull(it) }
            ?.let { EncryptionKey(PlainByteArray(it).encrypt(keyStoreCrypto)) }
        val childClientId = tokens.getNotBlankOrNull(2)?.let { ChildClientId(it) }

        return when {
            childClientId == null || encryptionKey == null || userCode == null -> null
            else -> EdmParams(childClientId, encryptionKey, userCode)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun Base64.decodeOrNull(s: String): ByteArray? = try {
        decode(s)
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun List<String>.getNotBlankOrNull(index: Int): String? = getOrNull(index)?.takeIfNotBlank()
}
