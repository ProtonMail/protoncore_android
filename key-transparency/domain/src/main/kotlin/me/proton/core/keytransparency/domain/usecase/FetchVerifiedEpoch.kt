/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.keytransparency.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.KeyTransparencyLogger
import me.proton.core.keytransparency.domain.entity.VerifiedEpochData
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

internal class FetchVerifiedEpoch @Inject constructor(
    private val keyTransparencyRepository: KeyTransparencyRepository,
    private val checkVerifiedEpochSignature: CheckVerifiedEpochSignature
) {

    suspend operator fun invoke(userId: UserId, userAddress: UserAddress): VerifiedEpochData? = runCatching {
        keyTransparencyRepository.getVerifiedEpoch(userId, userAddress.addressId)
    }.map { verifiedEpoch ->
        runCatching { checkVerifiedEpochSignature(userId, verifiedEpoch) }
            .onFailure {
                KeyTransparencyLogger.e(it, "Could not verify verified epoch signature")
                return null
            }
        runCatching { verifiedEpoch.getVerifiedEpoch() }
            .onFailure {
                KeyTransparencyLogger.e(it, "Could not parse verified epoch data")
            }
            .getOrNull()
    }.getOrElse { reason ->
        // Happens the first time the user runs KT
        if (
            reason is ApiException &&
            reason.error is ApiResult.Error.Http &&
            (reason.error as ApiResult.Error.Http).httpCode == HttpResponseCodes.HTTP_UNPROCESSABLE
        ) {
            // Will need to bootstrap the verified epoch
            return@getOrElse null
        } else {
            throw reason
        }
    }
}
