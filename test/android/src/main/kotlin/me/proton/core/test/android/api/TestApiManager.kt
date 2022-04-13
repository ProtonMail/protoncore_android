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

package me.proton.core.test.android.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.safeCall
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus

class TestApiManager<Api : BaseRetrofitApi>(
    private val api: Api,
    private val networkManager: NetworkManager = FakeNetworkManager(),
) : ApiManager<Api> {
    override suspend fun <T> invoke(
        forceNoRetryOnConnectionErrors: Boolean,
        block: suspend Api.() -> T,
    ): ApiResult<T> = api.safeCall(networkManager, block)
}

class FakeNetworkManager : NetworkManager() {
    override var networkStatus: NetworkStatus = NetworkStatus.Metered
    override fun register() {}
    override fun unregister() {}
}
