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

import io.sentry.Hint
import io.sentry.SentryEvent
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TimberTagEventFilterTest {
    private val emptyHint = Hint()
    private lateinit var tested: TimberTagEventFilter

    @Test
    fun `block none, allow none`() {
        tested = TimberTagEventFilter(allowedTagPrefixes = emptySet())

        assertNull(tested.process(event(), emptyHint))
        assertNull(tested.process(event("core.auth"), emptyHint))
        assertNull(tested.process(event("main.app"), emptyHint))
    }

    @Test
    fun `block none, allow some`() {
        tested = TimberTagEventFilter(allowedTagPrefixes = setOf("core.allowed"))

        assertNull(tested.process(event(), emptyHint))
        assertNull(tested.process(event("main.app"), emptyHint))
        assertNotNull(tested.process(event("core.allowed"), emptyHint))
    }

    @Test
    fun `block none, allow all`() {
        tested = TimberTagEventFilter()

        assertNotNull(tested.process(event(), emptyHint))
        assertNotNull(tested.process(event("core.auth"), emptyHint))
        assertNotNull(tested.process(event("app.main"), emptyHint))
    }

    @Test
    fun `block some, allow none`() {
        tested = TimberTagEventFilter(
            allowedTagPrefixes = emptySet(),
            blockedTagPrefixes = setOf("core.blocked")
        )

        assertNull(tested.process(event(), emptyHint))
        assertNull(tested.process(event("main.app"), emptyHint))
        assertNull(tested.process(event("core.blocked"), emptyHint))
    }

    @Test
    fun `block some, allow some`() {
        tested = TimberTagEventFilter(
            allowedTagPrefixes = setOf("core.allowed"),
            blockedTagPrefixes = setOf("core.blocked", "core.allowed.not")
        )

        assertNull(tested.process(event(), emptyHint))
        assertNull(tested.process(event("core.blocked"), emptyHint))
        assertNull(tested.process(event("core.allowed.not"), emptyHint))
        assertNull(tested.process(event("core.allowed.not.sub"), emptyHint))
        assertNull(tested.process(event("core.allowed.not_sub"), emptyHint))

        assertNotNull(tested.process(event("core.allowed"), emptyHint))
        assertNotNull(tested.process(event("core.allowed.sub"), emptyHint))
        assertNotNull(tested.process(event("core.allowed_sub"), emptyHint))
    }

    @Test
    fun `block some, allow all`() {
        tested = TimberTagEventFilter(blockedTagPrefixes = setOf("core.blocked"))

        assertNull(tested.process(event("core.blocked"), emptyHint))
        assertNull(tested.process(event("core.blocked.sub"), emptyHint))

        assertNotNull(tested.process(event(), emptyHint))
        assertNotNull(tested.process(event("core"), emptyHint))
        assertNotNull(tested.process(event("core.allowed"), emptyHint))
    }

    @Test
    fun `block all, allow none`() {
        tested = TimberTagEventFilter(
            allowedTagPrefixes = emptySet(),
            blockedTagPrefixes = setOf("")
        )

        assertNull(tested.process(event(), emptyHint))
        assertNull(tested.process(event("main.app"), emptyHint))
        assertNull(tested.process(event("core.blocked"), emptyHint))
    }

    @Test
    fun `block all, allow some`() {
        tested = TimberTagEventFilter(
            allowedTagPrefixes = setOf("core.allowed"),
            blockedTagPrefixes = setOf("")
        )

        assertNull(tested.process(event(), emptyHint))
        assertNull(tested.process(event("main.app"), emptyHint))
        assertNull(tested.process(event("core.allowed"), emptyHint))
    }

    @Test
    fun `block all, allow all`() {
        tested = TimberTagEventFilter(blockedTagPrefixes = setOf(""))

        assertNull(tested.process(event(), emptyHint))
        assertNull(tested.process(event("core.auth"), emptyHint))
        assertNull(tested.process(event("app.main"), emptyHint))
    }

    private fun event(timberTag: String? = null): SentryEvent =
        SentryEvent().apply {
            timberTag?.let { setTag(TIMBER_LOGGER_TAG, it) }
        }
}
