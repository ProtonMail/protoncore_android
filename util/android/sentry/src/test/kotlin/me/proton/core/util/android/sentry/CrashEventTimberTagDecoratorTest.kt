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
import io.sentry.protocol.Mechanism
import io.sentry.protocol.SentryException
import kotlin.test.Test
import kotlin.test.assertEquals

class CrashEventTimberTagDecoratorTest {
    private val emptyHint = Hint()
    private lateinit var tested: CrashEventTimberTagDecorator

    @Test
    fun `decorate all crashed events`() {
        tested = CrashEventTimberTagDecorator()

        assertEquals(
            TAG_UNCAUGHT_EXCEPTION,
            tested.process(
                event(exceptionModules = listOf("module1", "module2")),
                emptyHint
            ).getTag(TIMBER_LOGGER_TAG)
        )

        assertEquals(
            TAG_UNCAUGHT_EXCEPTION,
            tested.process(
                event(exceptionModules = listOf(null, "module2")),
                emptyHint
            ).getTag(TIMBER_LOGGER_TAG)
        )
    }

    @Test
    fun `decorate some crashed events`() {
        tested = CrashEventTimberTagDecorator(allowedModulePrefixes = setOf("allowed"))

        assertEquals(
            TAG_UNCAUGHT_EXCEPTION,
            tested.process(
                event(exceptionModules = listOf("module1", "allowed")),
                emptyHint
            ).getTag(TIMBER_LOGGER_TAG)
        )

        assertEquals(
            null,
            tested.process(
                event(exceptionModules = listOf("module1", null, "module2")),
                emptyHint
            ).getTag(TIMBER_LOGGER_TAG)
        )

        assertEquals(
            TAG_UNCAUGHT_EXCEPTION,
            tested.process(
                event(exceptionModules = listOf(null, "allowed")),
                emptyHint
            ).getTag(TIMBER_LOGGER_TAG)
        )
    }

    @Test
    fun `don't decorate handled exceptions`() {
        tested = CrashEventTimberTagDecorator()
        assertEquals(
            null,
            tested.process(
                event(isHandledException = true, exceptionModules = listOf("module1")),
                emptyHint
            ).getTag(TIMBER_LOGGER_TAG)
        )
    }

    @Test
    fun `don't decorate events tagged with timber tag`() {
        tested = CrashEventTimberTagDecorator()
        assertEquals(
            "original",
            tested.process(
                event(
                    originalTimberTag = "original",
                    exceptionModules = listOf("module1")
                ), emptyHint
            ).getTag(TIMBER_LOGGER_TAG)
        )
    }

    @Test
    fun `don't decorate if no exceptions`() {
        tested = CrashEventTimberTagDecorator()
        assertEquals(
            null,
            tested.process(
                event(exceptionModules = null),
                emptyHint
            ).getTag(TIMBER_LOGGER_TAG)
        )
    }

    private fun event(
        isHandledException: Boolean = false,
        originalTimberTag: String? = null,
        exceptionModules: List<String?>?
    ): SentryEvent =
        SentryEvent().apply {
            exceptions = exceptionModules?.map { moduleName ->
                SentryException().apply {
                    mechanism = Mechanism().apply { isHandled = isHandledException }
                    module = moduleName
                }
            }
            originalTimberTag?.let { setTag(TIMBER_LOGGER_TAG, it) }
        }
}