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

package me.proton.android.core.coreexample

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import me.proton.android.core.coreexample.api.CoreExampleRepository
import me.proton.android.core.coreexample.di.NetworkBindsModule
import me.proton.android.core.coreexample.di.NetworkCallbacksModule
import me.proton.android.core.coreexample.di.NetworkConstantsModule
import me.proton.core.network.dagger.CoreNetworkCryptoModule
import me.proton.core.network.data.di.AlternativeApiPins
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.data.di.CertificatePins
import me.proton.core.network.data.di.DohProviderUrls
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.DohProvider
import me.proton.core.network.domain.TimeoutOverride
import me.proton.core.network.domain.server.ServerClock
import me.proton.core.network.domain.server.ServerTimeManager
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.minidns.dnsmessage.DnsMessage
import org.minidns.record.Record
import org.minidns.record.TXT
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Config(application = HiltTestApplication::class)
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@UninstallModules(
    CoreNetworkCryptoModule::class,
    NetworkBindsModule::class,
    NetworkConstantsModule::class,
    NetworkCallbacksModule::class
    // New module for testing is defined in NetworkTests.TestNetworkModule
)
class NetworkTests {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var apiClient: FakeApiClient

    @Inject
    internal lateinit var repository: CoreExampleRepository

    private lateinit var apiServer: MockWebServer
    private lateinit var dohServer: MockWebServer

    @Before
    fun setUp() {
        DohProvider.lastAlternativesRefresh = Long.MIN_VALUE
        apiServer = MockWebServer().apply { start() }
        dohServer = MockWebServer().apply { start() }
        ApiPort = apiServer.port
        DohPort = dohServer.port

        hiltRule.inject()
    }

    @After
    fun tearDown() {
        apiServer.shutdown()
        dohServer.shutdown()
    }

    @Test
    fun error400() {
        apiServer.enqueue(MockResponse().setHeadersDelay(1000, TimeUnit.MILLISECONDS).setResponseCode(400))

        val result = ping()
        assertIs<ApiResult.Error.Http>(result)
        assertEquals(400, result.httpCode)
    }

    @Test
    fun error408() {
        apiServer.enqueue(
            MockResponse()
                .setHeadersDelay(100, TimeUnit.MILLISECONDS)
                .setResponseCode(408)
                .addHeader("Connection", "Close")
                .apply {
                    socketPolicy = SocketPolicy.DISCONNECT_AT_END
                }
        )

        apiServer.enqueue(
            MockResponse()
                .setHeadersDelay(100, TimeUnit.MILLISECONDS)
                .setResponseCode(200)
        )

        val result = ping()
        assertIs<ApiResult.Success<Unit>>(result)
    }

    @Test
    fun error429() {
        apiServer.enqueue(MockResponse().setHeadersDelay(100, TimeUnit.MILLISECONDS).setResponseCode(429))

        val result = ping()
        assertIs<ApiResult.Error.Http>(result)
        assertEquals(429, result.httpCode)
    }

    @Test
    fun error429WithShortRetryAfter() {
        apiServer.enqueue(
            MockResponse()
                .setHeadersDelay(100, TimeUnit.MILLISECONDS)
                .setResponseCode(429)
                .addHeader("Retry-After", "1")
        )

        apiServer.enqueue(MockResponse().setHeadersDelay(100, TimeUnit.MILLISECONDS).setResponseCode(200))

        val result = ping()
        assertIs<ApiResult.Success<Unit>>(result)
    }

    @Test
    fun error429WithLongRetryAfter() {
        apiServer.enqueue(
            MockResponse()
                .setHeadersDelay(100, TimeUnit.MILLISECONDS)
                .setResponseCode(429)
                .addHeader("Retry-After", "30")
        )

        val result = ping()
        assertIs<ApiResult.Error.Http>(result)
        assertEquals(429, result.httpCode)
    }

    @Test
    fun error500() {
        apiServer.enqueue(MockResponse().setHeadersDelay(100, TimeUnit.MILLISECONDS).setResponseCode(500))
        apiServer.enqueue(MockResponse().setHeadersDelay(100, TimeUnit.MILLISECONDS).setResponseCode(200))

        val result = ping()
        assertIs<ApiResult.Success<Unit>>(result)
    }

    @Test
    fun error503() {
        apiServer.enqueue(MockResponse().setHeadersDelay(100, TimeUnit.MILLISECONDS).setResponseCode(503))

        val result = ping()
        assertIs<ApiResult.Error.Http>(result)
        assertEquals(503, result.httpCode)
    }

    @Test
    fun error503WithShortRetryAfter() {
        apiServer.enqueue(
            MockResponse()
                .setHeadersDelay(100, TimeUnit.MILLISECONDS)
                .setResponseCode(503)
                .addHeader("Retry-After", "1")
        )
        apiServer.enqueue(MockResponse().setHeadersDelay(100, TimeUnit.MILLISECONDS).setResponseCode(200))

        val result = ping()
        assertIs<ApiResult.Success<Unit>>(result)
    }

    @Test
    fun error503WithLongRetryAfter() {
        apiServer.enqueue(
            MockResponse()
                .setHeadersDelay(100, TimeUnit.MILLISECONDS)
                .setResponseCode(503)
                .addHeader("Retry-After", "30")
        )

        val result = ping()
        assertIs<ApiResult.Error.Http>(result)
        assertEquals(503, result.httpCode)
    }

    @Test
    fun noResponse() {
        apiServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val result = ping()
        assertIs<ApiResult.Error.Timeout>(result)
    }

    @Test
    @Ignore
    fun doh() {
        apiClient.shouldUseDoh = true

        apiServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().apply {
                    setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
                }
            }
        }

        dohServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withDnsResponse()
            }
        }

        val result = ping()
        assertIs<ApiResult.Success<Unit>>(result)
    }

    @Test
    @Ignore
    fun guestHole() {
        apiClient.shouldUseDoh = true

        apiServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return if (request.path?.startsWith("/dns-query") == true) {
                    MockResponse().withDnsResponse()
                } else {
                    MockResponse().apply {
                        setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
                    }
                }
            }
        }

        val result = ping()
        assertIs<ApiResult.Success<Unit>>(result)
    }

    private fun ping(): ApiResult<Unit> = runBlocking {
        repository.ping(TimeoutOverride(readTimeoutSeconds = 5))
    }

    private fun MockResponse.withDnsResponse(): MockResponse {
        val txtBytes = ALTERNATIVE_API_HOST.toByteArray()
        val txtBlob = byteArrayOf(txtBytes.size.toByte(), *txtBytes)
        val dnsMessage = DnsMessage.builder()
            .addAnswer(Record("", Record.TYPE.TXT, Record.CLASS.IN, 0L, TXT(txtBlob), false))
            .build()
        addHeader("Content-Type", "application/dns-message")
        setBodyDelay(100, TimeUnit.MILLISECONDS)
        setBody(Buffer().write(dnsMessage.toArray()))
        setResponseCode(200)
        return this
    }

    @Module
    @InstallIn(SingletonComponent::class)
    internal class TestNetworkModule {
        @BaseProtonApiUrl
        @Provides
        fun provideBaseProtonApiUrl(): HttpUrl = "http://localhost:$ApiPort".toHttpUrl()

        @Provides
        @Singleton
        fun provideApiClient(fakeApiClient: FakeApiClient): ApiClient = fakeApiClient

        @Provides
        @Singleton
        fun provideFakeApiClient(): FakeApiClient = FakeApiClient()

        @Provides
        @DohProviderUrls
        fun provideDohProviderUrls(): Array<String> = arrayOf("http://localhost:$DohPort/dns-query/")

        @Provides
        @Singleton
        fun provideFakeDohAlternativesListener() = FakeDohAlternativesListener()

        @Provides
        @Singleton
        fun provideDohAlternativesListener(listener: FakeDohAlternativesListener): DohAlternativesListener = listener

        @CertificatePins
        @Provides
        fun provideCertificatePins() = emptyArray<String>()

        @AlternativeApiPins
        @Provides
        fun provideAlternativeApiPins() = emptyList<String>()

        @Provides
        @Singleton
        fun provideServerTimeManager() = ServerTimeManager {}

        @Provides
        @Singleton
        fun provideServerClock(serverTimeManager: ServerTimeManager): ServerClock =
            ServerClock(serverTimeManager)
    }

    companion object {
        private const val ALTERNATIVE_API_HOST = "api.proton.black"
        private var ApiPort: Int = 0
        private var DohPort: Int = 0
    }
}

internal class FakeApiClient : ApiClient {
    var shouldUseDoh: Boolean = false
    override val appVersionHeader: String = "android-mail@1.2.3"
    override val userAgent: String = "Test/1.0 (Android 10; brand model)"
    override val enableDebugLogging: Boolean = false
    override val backoffRetryCount: Int = 1

    override fun forceUpdate(errorMessage: String) {}
    override suspend fun shouldUseDoh(): Boolean = shouldUseDoh
}

internal class FakeDohAlternativesListener : DohAlternativesListener {
    override suspend fun onAlternativesUnblock(alternativesBlockCall: suspend () -> Unit) {
        alternativesBlockCall()
    }

    override suspend fun onProxiesFailed() {}
}
