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

import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.protocol.Message
import io.sentry.protocol.SentryId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val FAKE_SENTRY_DSN = "https://d765@o450.fake-sentry.test/45054"

class SentryHubTest {
    @Test
    fun `null dsn`() {
        val sentryHub = SentryHub(SentryOptions())
        assertEquals(
            SentryId.EMPTY_ID,
            sentryHub.captureEvent(SentryEvent())
        )
    }

    @Test
    fun `empty dsn`() {
        val sentryHub = SentryHub(SentryOptions().apply {
            dsn = ""
        })
        assertEquals(
            SentryId.EMPTY_ID,
            sentryHub.captureEvent(SentryEvent())
        )
    }

    @Test
    fun `blank dsn`() {
        val sentryHub = SentryHub(SentryOptions().apply {
            dsn = "  "
        })
        assertEquals(
            SentryId.EMPTY_ID,
            sentryHub.captureEvent(SentryEvent())
        )
    }

    @Test
    fun `capture event`() {
        val sentryHub = SentryHub(SentryOptions().apply {
            dsn = FAKE_SENTRY_DSN
        })
        assertNotEquals(
            SentryId.EMPTY_ID,
            sentryHub.captureEvent(SentryEvent().apply {
                message = Message().apply { message = "test" }
            })
        )
    }

    @Test
    fun `capture event from different thread`() {
        val sentryHub = SentryHub(SentryOptions().apply {
            dsn = FAKE_SENTRY_DSN
        })

        runBlocking {
            launch(Dispatchers.Default) {
                assertNotEquals(
                    SentryId.EMPTY_ID,
                    sentryHub.captureEvent(SentryEvent().apply {
                        message = Message().apply { message = "test" }
                    })
                )
            }
        }
    }
}