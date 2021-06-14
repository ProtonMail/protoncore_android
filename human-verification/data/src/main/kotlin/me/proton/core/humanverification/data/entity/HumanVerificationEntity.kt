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

package me.proton.core.humanverification.data.entity

import androidx.room.Entity
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decryptOrElse
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdType
import me.proton.core.network.domain.client.CookieSessionId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.humanverification.VerificationMethod
import me.proton.core.network.domain.session.SessionId

@Entity(
    primaryKeys = ["clientId"]
)
data class HumanVerificationEntity(
    val clientId: String,
    val clientIdType: ClientIdType,
    val verificationMethods: List<String>,
    val captchaVerificationToken: String? = null,
    val state: HumanVerificationState,
    val humanHeaderTokenType: EncryptedString? = null,
    val humanHeaderTokenCode: EncryptedString? = null
) {
    fun toHumanVerificationDetails(keyStoreCrypto: KeyStoreCrypto) = HumanVerificationDetails(
        clientId = when (clientIdType) {
            ClientIdType.SESSION -> ClientId.AccountSession(SessionId(clientId))
            ClientIdType.COOKIE -> ClientId.CookieSession(CookieSessionId(clientId))
        },
        verificationMethods = verificationMethods.map { VerificationMethod.getByValue(it) },
        captchaVerificationToken = captchaVerificationToken,
        state = state,
        // Fall back to an invalid captcha to force delete token on decryption failure.
        // See HumanVerificationInvalidHandler and HumanVerificationListener.onHumanVerificationInvalid.
        tokenType = humanHeaderTokenType?.decryptOrElse(keyStoreCrypto) { "captcha" },
        tokenCode = humanHeaderTokenCode?.decryptOrElse(keyStoreCrypto) { "invalid" }
    )
}
