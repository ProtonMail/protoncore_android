/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.accountrecovery.data.repository

import me.proton.core.accountrecovery.data.api.AccountRecoveryApi
import me.proton.core.accountrecovery.data.api.request.CancelRecoveryAttemptRequest
import me.proton.core.accountrecovery.data.api.response.isSuccess
import me.proton.core.accountrecovery.domain.repository.AccountRecoveryRepository
import me.proton.core.auth.domain.usecase.ValidateServerProof
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import javax.inject.Inject

public class AccountRecoveryRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val validateServerProof: ValidateServerProof,
) : AccountRecoveryRepository {
    override suspend fun cancelRecoveryAttempt(
        srpProofs: SrpProofs,
        srpSession: String,
        userId: UserId
    ): Boolean {
        return apiProvider.get<AccountRecoveryApi>(userId).invoke {
            val request = CancelRecoveryAttemptRequest(
                srpProofs.clientEphemeral,
                srpProofs.clientProof,
                srpSession
            )
            val response = cancelRecoveryAttempt(request)

            validateServerProof(
                response.serverProof,
                srpProofs.expectedServerProof
            ) { "Cancelling recovery attempt failed." }

            response.isSuccess()
        }.valueOrThrow
    }
}
