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
package me.proton.core.network.data

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.client.ExtraHeaderProviderImpl
import me.proton.core.network.data.di.AlternativeApiPins
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.data.di.CertificatePins
import me.proton.core.network.data.di.Constants
import me.proton.core.network.data.di.DohProviderUrls
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.TimeoutOverride
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.deviceverification.DeviceVerificationListener
import me.proton.core.network.domain.deviceverification.DeviceVerificationProvider
import me.proton.core.network.domain.feature.FeatureDisabledListener
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.DispatcherProvider
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertIs

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
internal class PinningTests {

    interface TestApi : BaseRetrofitApi

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    internal val product: Product = Product.Mail

    @BindValue
    internal val apiClient: ApiClient = TestApiClient()

    @BindValue
    internal val dispatcherProvider: DispatcherProvider =
        TestDispatcherProvider(UnconfinedTestDispatcher())

    @BindValue
    internal val coroutineScopeProvider: CoroutineScopeProvider =
        TestCoroutineScopeProvider(dispatcherProvider)

    @BindValue
    internal val missingScopeListener: MissingScopeListener = mockk()

    @BindValue
    internal val humanVerificationListener: HumanVerificationListener = mockk()

    @BindValue
    internal val deviceVerificationProvider: DeviceVerificationProvider = mockk()

    @BindValue
    internal val deviceVerificationListener: DeviceVerificationListener = mockk()

    @BindValue
    internal val humanVerificationProvider: HumanVerificationProvider = mockk(relaxed = true)

    @BindValue
    internal val featureDisabledListener: FeatureDisabledListener = mockk()

    @BindValue
    internal val sessionListener: SessionListener = mockk()

    @BindValue
    internal val sessionProvider: SessionProvider = mockk {
        coEvery { getSessionId(null) } returns null
        coEvery { getSession(null) } returns null
    }

    @BindValue
    internal val extraHeaderProvider: ExtraHeaderProvider = ExtraHeaderProviderImpl()

    @BindValue
    internal val dohAlternativesListener: DohAlternativesListener = mockk(relaxed = true)

    @BindValue
    internal val cryptoContext: CryptoContext = mockk(relaxed = true)

    @BindValue
    @BaseProtonApiUrl
    internal lateinit var baseApiUrl: HttpUrl

    @BindValue
    @CertificatePins
    internal lateinit var certificatePins: Array<String>

    @BindValue
    @AlternativeApiPins
    internal lateinit var alternativeApiPins: List<String>

    @BindValue
    @DohProviderUrls
    internal lateinit var dohProviderUrls: Array<String>

    @Inject
    internal lateinit var apiProvider: ApiProvider

    private suspend fun assertSuccess(baseApiUrl: String) {
        // GIVEN
        this.baseApiUrl = baseApiUrl.toHttpUrl()
        this.certificatePins = Constants.DEFAULT_SPKI_PINS
        this.alternativeApiPins = Constants.ALTERNATIVE_API_SPKI_PINS
        this.dohProviderUrls = Constants.DOH_PROVIDERS_URLS
        hiltRule.inject()

        // WHEN
        apiProvider.get<TestApi>().invoke {
            ping(TimeoutOverride(5, 5, 5))
        }.valueOrThrow
    }

    private suspend fun assertFailure(baseApiUrl: String) {
        // GIVEN
        this.baseApiUrl = baseApiUrl.toHttpUrl()
        this.certificatePins = arrayOf("fakefakefakefakefakefakefakefakfakefakefake=")
        this.alternativeApiPins = listOf("fakefakefakefakefakefakefakefakfakefakefake=")
        this.dohProviderUrls = Constants.DOH_PROVIDERS_URLS
        hiltRule.inject()

        // WHEN
        val result = apiProvider.get<TestApi>().invoke {
            ping(TimeoutOverride(5, 5, 5))
        }

        // THEN
        assertIs<ApiResult.Error.Certificate>(result)
    }

    @Test
    fun `calling api protonmail com, failure`() = runTest {
        assertFailure(baseApiUrl = "https://api.protonmail.ch")
    }

    @Test
    fun `calling api protonmail ch, success`() = runTest {
        assertSuccess(baseApiUrl = "https://api.protonmail.ch")
    }

    @Test
    fun `calling api protonvpn ch, success`() = runTest {
        assertSuccess(baseApiUrl = "https://api.protonvpn.ch")
    }

    @Test
    fun `calling verify protonvpn com, success`() = runTest {
        assertSuccess(baseApiUrl = "https://verify.protonvpn.com")
    }

    @Test
    fun `calling proton me, success`() = runTest(dispatcherProvider.Main) {
        assertSuccess(baseApiUrl = "https://verify.proton.me")
    }

    @Test
    fun `calling verify api protonvpn com, success`() = runTest {
        assertSuccess(baseApiUrl = "https://verify-api.protonvpn.com")
    }

    @Test
    fun `calling verify api proton me, success`() = runTest {
        assertSuccess(baseApiUrl = "https://verify-api.proton.me")
    }

    @Test
    fun `calling mail api proton me, success`() = runTest {
        assertSuccess(baseApiUrl = "https://mail-api.proton.me")
    }

    @Test
    fun `calling drive api proton me, success`() = runTest {
        assertSuccess(baseApiUrl = "https://drive-api.proton.me")
    }

    @Test
    fun `calling calendar api proton me, success`() = runTest {
        assertSuccess(baseApiUrl = "https://calendar-api.proton.me")
    }

    @Test
    fun `calling vpn api proton me, success`() = runTest {
        assertSuccess(baseApiUrl = "https://vpn-api.proton.me")
    }
}
