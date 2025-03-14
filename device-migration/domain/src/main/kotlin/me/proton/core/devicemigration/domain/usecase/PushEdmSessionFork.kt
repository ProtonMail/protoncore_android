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

import me.proton.core.auth.domain.usecase.ForkSession
import me.proton.core.auth.domain.usecase.Selector
import me.proton.core.auth.domain.usecase.fork.GetEncryptedPassphrasePayload
import me.proton.core.devicemigration.domain.entity.EdmParams
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

public class PushEdmSessionFork @Inject constructor(
    private val forkSession: ForkSession,
    private val getEncryptedPassphrasePayload: GetEncryptedPassphrasePayload,
) {
    public suspend operator fun invoke(
        userId: UserId,
        params: EdmParams
    ): Selector = forkSession(
        userId = userId,
        payload = getEncryptedPassphrasePayload(
            userId = userId,
            encryptionKey = params.encryptionKey.value,
            aesCipherGCMTagBits = EDM_AES_CIPHER_GCM_TAG_BITS,
            aesCipherIvBytes = EDM_AES_CIPHER_IV_BYTES
        ),
        childClientId = params.childClientId.value,
        independent = true,
        userCode = params.userCode.value
    )
}
