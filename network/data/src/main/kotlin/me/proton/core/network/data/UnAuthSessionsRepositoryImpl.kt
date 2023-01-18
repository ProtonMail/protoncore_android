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

package me.proton.core.network.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.challenge.data.frame.ChallengeFrame
import me.proton.core.challenge.domain.framePrefix
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.RequestTokenRequest
import me.proton.core.network.domain.UnAuthSessionsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnAuthSessionsRepositoryImpl @Inject constructor(
    private val provider: ApiProvider,
    @ApplicationContext private val context: Context,
    private val product: Product
) : UnAuthSessionsRepository {

    /**
     * Requests a new unauthenticated token from the API directly.
     */
    override suspend fun requestToken() =
        provider.get<BaseRetrofitApi>().invoke {
            val name = "${product.framePrefix()}-0"
            val frame = ChallengeFrame.Device.build(context)
            requestToken(
                RequestTokenRequest(
                    mapOf(name to frame)
                )
            ).toSession()
        }.valueOrThrow
}