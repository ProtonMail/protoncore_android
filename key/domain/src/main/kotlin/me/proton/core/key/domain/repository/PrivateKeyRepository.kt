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

package me.proton.core.key.domain.repository

import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.key.domain.entity.key.Key
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

@ExcludeFromCoverage // excluded because of the default value in updatePrivateKeys. we do not test interfaces.
interface PrivateKeyRepository {

    /**
     * Create new primary Keys using [sessionUserId], remotely.
     */
    suspend fun setupInitialKeys(
        sessionUserId: SessionUserId,
        primaryKey: Armored,
        primaryKeySalt: String,
        addressKeys: List<PrivateAddressKey>,
        auth: Auth
    )

    /**
     * Create a new address key with the info provided in [key] using [sessionUserId], remotely.
     */
    suspend fun createAddressKey(
        sessionUserId: SessionUserId,
        key: PrivateAddressKey
    )

    suspend fun updatePrivateKeys(
        sessionUserId: SessionUserId,
        keySalt: String,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorProof: SecondFactorProof?,
        auth: Auth?,
        keys: List<Key>? = null,
        userKeys: List<Key>? = null
    ): Boolean

    suspend fun reactivatePrivateKey(
        sessionUserId: SessionUserId,
        privateKeyId: String,
        privateKey: PrivateKey,
        addressKeysFingerprints: List<String>,
        signedKeyLists: Map<String, PublicSignedKeyList>
    ): Boolean
}
