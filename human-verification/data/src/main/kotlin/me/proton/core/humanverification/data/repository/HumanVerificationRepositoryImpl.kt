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

package me.proton.core.humanverification.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encryptWith
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.humanverification.data.entity.HumanVerificationEntity
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.getType

class HumanVerificationRepositoryImpl(
    private val db: HumanVerificationDatabase,
    private val keyStoreCrypto: KeyStoreCrypto
) : HumanVerificationRepository {

    private val humanVerificationDetailsDao = db.humanVerificationDetailsDao()
    private val humanVerificationStateChanged = MutableSharedFlow<HumanVerificationDetails>(extraBufferCapacity = 10)

    private fun tryEmitStateChanged(humanVerificationDetails: HumanVerificationDetails) {
        if (!humanVerificationStateChanged.tryEmit(humanVerificationDetails)) {
            throw IllegalStateException("Too many nested state changes, extra buffer capacity exceeded.")
        }
    }

    override suspend fun getHumanVerificationDetails(clientId: ClientId): HumanVerificationDetails? =
        humanVerificationDetailsDao.getByClientId(clientId.id)?.toHumanVerificationDetails(keyStoreCrypto)

    override suspend fun getAllHumanVerificationDetails(): Flow<List<HumanVerificationDetails>> =
        humanVerificationDetailsDao.getAll()
            .map { entity -> entity.map { it.toHumanVerificationDetails(keyStoreCrypto) } }
            .distinctUntilChanged()

    override suspend fun insertHumanVerificationDetails(details: HumanVerificationDetails) {
        db.inTransaction {
            val clientId = details.clientId
            humanVerificationDetailsDao.insertOrUpdate(
                HumanVerificationEntity(
                    clientId = clientId.id,
                    clientIdType = clientId.getType(),
                    verificationMethods = details.verificationMethods.map { method -> method.value },
                    captchaVerificationToken = details.captchaVerificationToken,
                    state = details.state,
                    humanHeaderTokenType = details.tokenType?.encryptWith(keyStoreCrypto),
                    humanHeaderTokenCode = details.tokenCode?.encryptWith(keyStoreCrypto)
                )
            )
            getHumanVerificationDetails(clientId)?.let { tryEmitStateChanged(it) }
        }
    }

    override suspend fun updateHumanVerificationState(
        clientId: ClientId,
        state: HumanVerificationState,
        tokenType: String?,
        tokenCode: String?
    ) {
        db.inTransaction {
            humanVerificationDetailsDao.updateStateAndToken(
                clientId.id,
                state,
                tokenType?.encryptWith(keyStoreCrypto),
                tokenCode?.encryptWith(keyStoreCrypto)
            )
            getHumanVerificationDetails(clientId)?.let { tryEmitStateChanged(it) }
        }
    }

    override fun onHumanVerificationStateChanged(
        initialState: Boolean
    ): Flow<HumanVerificationDetails> =
        humanVerificationStateChanged.asSharedFlow()
            .onSubscription { if (initialState) getAllHumanVerificationDetails().first().forEach { emit(it) } }
            .distinctUntilChanged()
}
