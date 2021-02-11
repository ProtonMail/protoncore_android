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

package me.proton.core.key.data.repository

import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.key.data.api.KeyApi
import me.proton.core.key.data.api.request.AuthRequest
import me.proton.core.key.data.api.request.CreateAddressKeyRequest
import me.proton.core.key.data.api.request.SetupInitialKeysRequest
import me.proton.core.key.data.api.request.SignedKeyListRequest
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.util.kotlin.toInt

class PrivateKeyRepositoryImpl(
    private val provider: ApiProvider
) : PrivateKeyRepository {

    override suspend fun setupInitialKeys(
        sessionUserId: SessionUserId,
        primaryKey: Armored,
        primaryKeySalt: String,
        primaryAddressId: String,
        primaryAddressPrivateKey: Armored,
        primaryAddressSignedKeyList: PublicSignedKeyList,
        primaryAddressToken: Armored?,
        primaryAddressSignature: Armored?,
        auth: Auth
    ) {
        return provider.get<KeyApi>(sessionUserId).invoke {
            setupInitialKeys(
                SetupInitialKeysRequest(
                    primaryKey = primaryKey,
                    keySalt = primaryKeySalt,
                    auth = AuthRequest.from(auth),
                    addressKeys = listOf(
                        CreateAddressKeyRequest(
                            addressId = primaryAddressId,
                            privateKey = primaryAddressPrivateKey,
                            primary = 1,
                            token = primaryAddressToken,
                            signature = primaryAddressSignature,
                            signedKeyList = SignedKeyListRequest(
                                primaryAddressSignedKeyList.data,
                                primaryAddressSignedKeyList.signature
                            )
                        )
                    )
                )
            )
        }.throwIfError()
    }

    override suspend fun createAddressKey(
        sessionUserId: SessionUserId,
        addressId: String,
        privateKey: Armored,
        primary: Boolean,
        signedKeyList: PublicSignedKeyList,
        token: Armored?,
        signature: Armored?
    ) {
        return provider.get<KeyApi>(sessionUserId).invoke {
            // TODO: Key Migration: call createAddressKey.
            createAddressKeyOld(
                CreateAddressKeyRequest(
                    addressId = addressId,
                    privateKey = privateKey,
                    primary = primary.toInt(),
                    token = token,
                    signature = signature,
                    signedKeyList = SignedKeyListRequest(
                        signedKeyList.data,
                        signedKeyList.signature
                    )
                )
            )
        }.throwIfError()
    }
}
