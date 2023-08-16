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

package me.proton.core.util.android.sentry

import android.content.Context
import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkPrefs
import timber.log.Timber
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SentryHubBuilderTest {

    private val context = mockk<Context>(relaxed = true)
    private val apiClient = mockk<ApiClient>(relaxed = true)
    private val networkPrefs = mockk<NetworkPrefs>(relaxed = true)

    private lateinit var tested: SentryHubBuilder

    @BeforeTest
    fun setUp() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk {
            every { thread } returns Thread.currentThread()
        }
        tested = SentryHubBuilder()
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `installs TimberLoggerSentryTree`() {
        // WHEN
        tested(context = context, apiClient = apiClient, sentryDsn = "", networkPrefs = networkPrefs)

        // THEN
        assertEquals(1, Timber.treeCount)
        assertIs<TimberLoggerSentryTree>(Timber.forest().first())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot overwrite environment option`() {
        tested(
            context = context,
            apiClient = apiClient,
            sentryDsn = "",
            envName = "test",
            networkPrefs = networkPrefs
        ) {
            it.environment = "changed"
        }
    }

    @Test
    fun `can set environment option`() {
        tested(context = context, apiClient = apiClient, sentryDsn = "", envName = null, networkPrefs = networkPrefs) {
            it.environment = "test"
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot overwrite release option`() {
        tested(
            context = context,
            apiClient = apiClient,
            sentryDsn = "",
            releaseName = "test",
            networkPrefs = networkPrefs
        ) {
            it.release = "changed"
        }
    }

    @Test
    fun `can set release option`() {
        tested(
            context = context,
            apiClient = apiClient,
            sentryDsn = "",
            releaseName = null,
            networkPrefs = networkPrefs
        ) {
            it.release = "test"
        }
    }
}
