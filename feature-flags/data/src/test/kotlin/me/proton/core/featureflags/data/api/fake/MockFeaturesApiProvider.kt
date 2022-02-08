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

package me.proton.core.featureflags.data.api.fake

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.featureflags.data.api.FeaturesApi
import me.proton.core.featureflags.data.api.response.FeatureApiResponse
import me.proton.core.featureflags.data.testdata.SessionIdTestData
import me.proton.core.featureflags.data.testdata.UserIdTestData
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiManagerImpl
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionProvider

/**
 * Allows mocking the response returned from [FeaturesApi]
 * @param expectedResponse the response that an invocation to `featuresApi.getFeatureFlag` will return.
 * Calls are expected to be done as follows:
 * ```
 * apiProvider.get<FeaturesApi>(userId).invoke { getFeatureFlag(feature.id) }
 * ```
 * This is needed as apiProvider behavior can't be directly mocked as
 * implemented in inline functions.
 * This class exposes an instance of `apiProvider` (through [mockedApiProvider]) which
 * dependencies have been mocked to return a given response.
 *
 * **Improvement**
 * This class might be improved by finding a way to have [apiManagerFactory] returning a mocked
 * instance of `ApiManager<FeaturesApi>>` instead of returning a real object with mocked dependencies
 */
class MockFeaturesApiProvider(expectedResponse: FeatureApiResponse) {

    private val primaryBackend = mockk<ApiBackend<FeaturesApi>> {
        coEvery { this@mockk.invoke<FeatureApiResponse>(any()) } returns ApiResult.Success(expectedResponse)
    }
    private val featuresApiManager = ApiManagerImpl(
        client = mockk(relaxed = true),
        primaryBackend = primaryBackend,
        errorHandlers = mockk(relaxed = true),
        monoClockMs = { 1 }
    )
    private val sessionProvider = mockk<SessionProvider> {
        coEvery { this@mockk.getSessionId(UserIdTestData.userId) } returns SessionIdTestData.sessionId
    }
    private val apiManagerFactory = mockk<ApiManagerFactory> {
        coEvery { this@mockk.create(SessionIdTestData.sessionId, FeaturesApi::class) } returns featuresApiManager
    }

    private val apiProvider = ApiProvider(apiManagerFactory, sessionProvider)

    fun mockedApiProvider() = apiProvider

    fun verifyApiProviderNotCalled() {
        verify { sessionProvider wasNot Called }
        verify { apiManagerFactory wasNot Called }
    }
}
