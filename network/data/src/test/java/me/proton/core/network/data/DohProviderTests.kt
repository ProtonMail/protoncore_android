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

import android.os.Build
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import me.proton.core.network.data.doh.DnsOverHttpsProviderRFC8484
import me.proton.core.network.data.util.MockApiClient
import me.proton.core.network.data.util.MockNetworkPrefs
import me.proton.core.network.data.util.takeRequestWithDefaultTimeout
import me.proton.core.network.domain.DohProvider
import me.proton.core.network.domain.DohService
import me.proton.core.network.domain.NetworkManager
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.runner.RunWith
import org.minidns.dnsmessage.DnsMessage
import org.minidns.record.Record
import org.minidns.record.TXT
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private val primaryService = DohService { _, _ -> delay(100); listOf("1.1.1.1")}
private val primaryServiceFail = DohService { _, _ -> delay(1000); null }
private val secondaryService = DohService { _, _ -> delay(10); listOf("2.2.2.2") }
private val secondaryServiceFail = DohService { _, _ -> delay(1000); null }
private const val SECONDARY_URL = "https://secondary.com"
private const val SECONDARY_URL_FAIL = "https://secondaryFail.com"

@Config(sdk = [Build.VERSION_CODES.M])
@RunWith(RobolectricTestRunner::class)
internal class DohProviderTests {

    private val domain = "example.com"
    lateinit var webServer: MockWebServer
    lateinit var dohProvider: DnsOverHttpsProviderRFC8484

    private var isNetworkAvailable = true

    @MockK
    lateinit var networkManager: NetworkManager

    @BeforeTest
    fun before() {
        MockKAnnotations.init(this)
        every { networkManager.isConnectedToNetwork() } answers { isNetworkAvailable }

        isNetworkAvailable = true
        webServer = MockWebServer()
        DohProvider.lastAlternativesRefresh = Long.MIN_VALUE
        val okHttpClient = OkHttpClient.Builder().build()
        val client = MockApiClient()
        dohProvider = DnsOverHttpsProviderRFC8484(
            okHttpClient,
            webServer.url("/").toString(),
            client,
            networkManager
        )
    }

    @AfterTest
    fun after() {
        webServer.shutdown()
    }

    @Test
    fun `test ok call`() = runTest {
        val txtBytes = "proxy.com".toByteArray()
        val txtBlob = byteArrayOf(txtBytes.size.toByte(), *txtBytes)
        val dnsMessage = DnsMessage.builder()
            .addAnswer(Record("", Record.TYPE.TXT, Record.CLASS.IN, 0L, TXT(txtBlob), false))
            .build()
        val response = MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/dns-message")
            .setBody(Buffer().write(dnsMessage.toArray()))
        webServer.enqueue(response)

        val result = dohProvider.getAlternativeBaseUrls(null, "https://$domain/")!!
        assertEquals(listOf("https://proxy.com/"), result)
        val request = webServer.takeRequestWithDefaultTimeout()
        val requestPath = request?.path
        val acceptHeader = request?.headers?.get("Accept")
        assertEquals("application/dns-message", acceptHeader)
        assertEquals("/?dns=AAABAAABAAAAAAAAE2RtdjRnYzNscW5yc3M0eTNwbnUJcHJvdG9ucHJvA3h5egAAEAAB", requestPath)
    }

    @Test
    fun `test services called in parallel`() = runTest {
        val service1 = mockk<DohService>()
        coEvery { service1.getAlternativeBaseUrls(any(), any()) } coAnswers {
            delay(10_000)
            null
        }

        val service2 = mockk<DohService>()
        coEvery { service2.getAlternativeBaseUrls(any(), any()) } coAnswers {
            delay(1000)
            listOf("proxy.com")
        }

        val prefs = MockNetworkPrefs()
        val provider = createDohProvider(
            listOf(service1, service2),
            emptyList(),
            { url -> mockk<DohService>() },
            prefs,
        )

        val start = currentTime
        provider.refreshAlternatives()
        val duration = currentTime - start

        coVerify(exactly = 1) { service1.getAlternativeBaseUrls(any(), any()) }
        coVerify(exactly = 1) { service2.getAlternativeBaseUrls(any(), any()) }
        assertEquals(listOf("proxy.com"), prefs.alternativeBaseUrls)

        // Make sure it finishes as soon as one service succeeds.
        assertEquals(1000, duration)
    }

    @Test
    fun `primary providers results are preferred to secondary ones`() = runTest {
        val prefs = MockNetworkPrefs()
        val provider = createDohProvider(
            listOf(primaryService),
            listOf(SECONDARY_URL, SECONDARY_URL_FAIL),
            { url ->
                when (url) {
                    SECONDARY_URL -> secondaryService
                    SECONDARY_URL_FAIL -> secondaryServiceFail
                    else -> throw IllegalStateException("Unexpected url")
                }
            },
            prefs,
        )

        val start = currentTime
        provider.refreshAlternatives()
        val duration = currentTime - start

        assertEquals(listOf("1.1.1.1"), prefs.alternativeBaseUrls)

        // We'll wait for the primary service to finish, even if secondary one finished already.
        assertEquals(100, duration)
        assertEquals(SECONDARY_URL, prefs.successfulSecondaryDohServiceUrl)
    }

    @Test
    fun `when primary providers fail, secondary results are used` () = runTest {
        val prefs = MockNetworkPrefs()
        val provider = createDohProvider(
            listOf(primaryServiceFail),
            listOf(SECONDARY_URL),
            { secondaryService },
            prefs,
        )

        val start = currentTime
        provider.refreshAlternatives()
        val duration = currentTime - start

        assertEquals(listOf("2.2.2.2"), prefs.alternativeBaseUrls)
        assertEquals(SECONDARY_URL, prefs.successfulSecondaryDohServiceUrl)
        // Wait for primary to fail (1000ms) and only then take secondary result.
        assertEquals(1000, duration)
    }

    @Test
    fun `when successful secondary fails, pref is cleared`() = runTest {
        val prefs = MockNetworkPrefs()
        prefs.successfulSecondaryDohServiceUrl = SECONDARY_URL_FAIL

        val primaryServiceLong = DohService { _, _ -> delay(2_000); listOf("1.1.1.1") }

        val provider = createDohProvider(
            listOf(primaryServiceLong),
            listOf(SECONDARY_URL_FAIL),
            { secondaryServiceFail },
            prefs,
        )

        val start = currentTime
        provider.refreshAlternatives()
        val duration = currentTime - start

        assertEquals(null, prefs.successfulSecondaryDohServiceUrl)
        assertEquals(listOf("1.1.1.1"), prefs.alternativeBaseUrls)
        assertEquals(2_000, duration)
    }

    @Test
    fun `when primary works it doesn't wait for slow secondary`() = runTest {
        val prefs = MockNetworkPrefs()
        val secondaryServiceSlow = DohService { _, _ -> delay(5_000); listOf("2.2.2.2") }
        val provider = createDohProvider(
            listOf(primaryService),
            listOf(SECONDARY_URL),
            { secondaryServiceSlow },
            prefs,
        )

        val start = currentTime
        provider.refreshAlternatives()
        val duration = currentTime - start

        assertEquals(listOf("1.1.1.1"), prefs.alternativeBaseUrls)
        assertEquals(100, duration)
    }

    private fun TestScope.createDohProvider(
        primaryServices: List<DohService> = listOf(primaryService),
        secondaryServicesUrls: List<String> = listOf(SECONDARY_URL),
        createSecondaryService: (String) -> DohService = { secondaryService },
        prefs: MockNetworkPrefs = MockNetworkPrefs(),
    ) = DohProvider(
        "https://test.com",
        MockApiClient(),
        primaryServices,
        secondaryServicesUrls,
        createSecondaryService,
        mockk<DohService>(),
        this,
        prefs,
        ::currentTime,
        null,
        null
    )
}
