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

import me.proton.core.auth.domain.entity.SessionForkUserCode
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.devicemigration.domain.entity.ChildClientId
import me.proton.core.devicemigration.domain.entity.EdmParams
import me.proton.core.devicemigration.domain.entity.EncryptionKey
import me.proton.core.util.kotlin.coroutine.result
import me.proton.core.util.kotlin.takeIfNotBlank
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

public class DecodeEdmCode @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto
) {
    /**
     * Format: `Version:UserCode:b64(EncryptionKey):ChildClientID`
     * where b64(EncryptionKey) is optional (can be an empty string).
     */
    public suspend operator fun invoke(encoded: String): EdmParams? = result("decodeEdmCode") {
        val tokens = encoded.split(":")
        val qrCodeVersion = tokens.getNotBlankOrNull(0)?.toIntOrNull()
        when (qrCodeVersion) {
            0 -> handleVersion0(tokens)
            else -> DecodeResult.VersionNotSupported
        }
    }.let { decodeResult ->
        (decodeResult as? DecodeResult.Success)?.params
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun handleVersion0(tokens: List<String>): DecodeResult {
        val userCode = tokens.getNotBlankOrNull(1)?.let { SessionForkUserCode(it) }
        val encryptionKey = tokens.getNotBlankOrNull(2)?.let {
            Base64.decodeCatching(it).map { result ->
                EncryptionKey(PlainByteArray(result).encrypt(keyStoreCrypto))
            }
        }
        val childClientId = tokens.getNotBlankOrNull(3)?.let { ChildClientId(it) }

        return when {
            childClientId == null -> DecodeResult.ChildClientIdMissing
            userCode == null -> DecodeResult.UserCodeMissing
            encryptionKey?.isFailure == true -> DecodeResult.EncryptionKeyDecodeFailure
            else -> DecodeResult.Success(
                EdmParams(
                    childClientId = childClientId,
                    encryptionKey = encryptionKey?.getOrThrow(),
                    userCode = userCode
                )
            )
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun Base64.decodeCatching(s: String): Result<ByteArray> = try {
        Result.success(decode(s))
    } catch (e: IllegalArgumentException) {
        Result.failure(e)
    }

    private fun List<String>.getNotBlankOrNull(index: Int): String? = getOrNull(index)?.takeIfNotBlank()

    public sealed interface DecodeResult {
        public data object ChildClientIdMissing : DecodeResult
        public data object EncryptionKeyDecodeFailure : DecodeResult
        public data class Success(val params: EdmParams?) : DecodeResult
        public data object UserCodeMissing : DecodeResult
        public data object VersionNotSupported : DecodeResult
    }
}
