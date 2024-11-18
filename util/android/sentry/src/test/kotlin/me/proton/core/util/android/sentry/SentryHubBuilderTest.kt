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

import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import timber.log.Timber
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SentryHubBuilderTest {
    private val customSentryTagsProcessor = mockk<CustomSentryTagsProcessor>()

    private lateinit var tested: SentryHubBuilder

    @BeforeTest
    fun setUp() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk {
            every { thread } returns Thread.currentThread()
        }
        tested = SentryHubBuilder(customSentryTagsProcessor)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `installs TimberLoggerSentryTree`() {
        // WHEN
        tested(sentryDsn = "")

        // THEN
        assertEquals(1, Timber.treeCount)
        assertIs<TimberLoggerSentryTree>(Timber.forest().first())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot overwrite environment option`() {
        tested(
            sentryDsn = "",
            envName = "test"
        ) {
            it.environment = "changed"
        }
    }

    @Test
    fun `can set environment option`() {
        tested(sentryDsn = "", envName = null) {
            it.environment = "test"
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot overwrite release option`() {
        tested(
            sentryDsn = "",
            releaseName = "test"
        ) {
            it.release = "changed"
        }
    }

    @Test
    fun `can set release option`() {
        tested(
            sentryDsn = "",
            releaseName = null
        ) {
            it.release = "test"
        }
    }
}
