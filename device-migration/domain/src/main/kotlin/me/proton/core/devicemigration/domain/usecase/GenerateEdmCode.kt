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

import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.devicemigration.domain.entity.ChildClientId
import me.proton.core.devicemigration.domain.entity.EdmCodeResult
import me.proton.core.devicemigration.domain.entity.EdmParams
import me.proton.core.devicemigration.domain.entity.EncryptionKey
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.applicationName
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

public class GenerateEdmCode @Inject constructor(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
    private val cryptoContext: CryptoContext,
) {
    private val pgpCrypto = cryptoContext.pgpCrypto

    /**
     * @param withEncryptionKey If `true`, a random encryption key will be included in the generated QR code.
     *  If `false` (e.g. on VPN-TV), no encryption key will be included, which also means that
     *  the origin device will not include the passphrase in fork's payload.
     */
    @OptIn(ExperimentalEncodingApi::class)
    public suspend operator fun invoke(
        sessionId: SessionId?,
        withEncryptionKey: Boolean = true
    ): EdmCodeResult {
        val (selector, userCode) = authRepository.getSessionForks(sessionId)
        val encryptionKey = when {
            withEncryptionKey -> PlainByteArray(pgpCrypto.generateRandomBytes(size = 32))
            else -> null
        }
        val encodedEncryptionKey = encryptionKey?.let { Base64.encode(it.array) }.orEmpty()
        val childClientId = ChildClientId(apiClient.applicationName)
        val edmParams = EdmParams(
            childClientId = childClientId,
            encryptionKey = encryptionKey?.let { EncryptionKey(it.encrypt(cryptoContext.keyStoreCrypto)) },
            userCode = userCode
        )
        val qrCode = "$EDM_QR_CODE_VERSION:${userCode.value}:${encodedEncryptionKey}:${childClientId.value}"
        return EdmCodeResult(
            edmParams = edmParams,
            qrCodeContent = qrCode,
            selector = selector
        )
    }
}
