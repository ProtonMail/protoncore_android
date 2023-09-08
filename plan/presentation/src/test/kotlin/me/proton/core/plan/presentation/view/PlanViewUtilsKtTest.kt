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

package me.proton.core.plan.presentation.view

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.util.kotlin.CoreLogger
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class PlanViewUtilsKtTest {

    private val typedArray = mockk<TypedArray>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val resources = mockk<Resources>(relaxed = true)

    private val testResourceId = 1

    @Before
    fun beforeEveryTest() {
        mockkObject(CoreLogger)
        mockkStatic("me.proton.core.plan.presentation.view.PlanViewUtilsKt")
        every { context.resources } returns resources
        every { typedArray.getResourceId(any(), 0) } returns testResourceId
    }

    @After
    fun afterEveryTest() {
        unmockkObject(CoreLogger)
        unmockkStatic("me.proton.core.plan.presentation.view.PlanViewUtilsKt")
    }

    @Test
    fun `get string array returns non null`() {
        // GIVEN
        val testResourceName = "test-name"
        every { resources.getIdentifier(testResourceName, "array", any()) } returns testResourceId
        every { resources.getStringArray(testResourceId) } returns arrayOf("first", "second")
        // WHEN
        val result = context.getStringArrayByName(testResourceName)
        // THEN
        assertNotNull(result)
        verify(exactly = 0) { CoreLogger.e(any(), any<Throwable>(), any<String>()) }
        verify(exactly = 0) { CoreLogger.e(any(), any<String>()) }
    }

    @Test
    fun `get string array returns non null mock string array`() {
        // GIVEN
        val testResourceName = "test-name"
        every { context.getStringArrayByName(testResourceId) } returns arrayOf("first", "second")
        // WHEN
        val result = context.getStringArrayByName(testResourceName)
        // THEN
        assertNotNull(result)
        verify(exactly = 0) { CoreLogger.e(any(), any<Throwable>(), any<String>()) }
        verify(exactly = 0) { CoreLogger.e(any(), any<String>()) }
    }

    @Test
    fun `get string array returns null mock string array`() {
        // GIVEN
        val testResourceName = "test-name"
        every { context.getStringArrayByName(testResourceId) } returns null
        // WHEN
        val result = context.getStringArrayByName(testResourceName)
        // THEN
        assertNotNull(result)
        verify(exactly = 0) { CoreLogger.e(any(), any<Throwable>(), any<String>()) }
        verify(exactly = 0) { CoreLogger.e(any(), any<String>()) }
    }

    @Test
    fun `get string array returns null and log is invoked`() {
        // GIVEN
        val testResourceName = "test-name"
        every { resources.getIdentifier(testResourceName, "array", any()) } returns testResourceId
        every { resources.getStringArray(testResourceId) } throws Resources.NotFoundException("Not found")

        val result = context.getStringArrayByName(testResourceName)
        assertNull(result)
        verify(exactly = 1) {
            CoreLogger.e(
                LogTag.PLAN_RESOURCE_ERROR,
                any(),
                "Plan config resource not found $testResourceName"
            )
        }
    }

    @Test
    fun `get integer array returns non null`() {
        // GIVEN
        val testResourceName = "test-name"
        every { resources.getIdentifier(testResourceName, "array", any()) } returns testResourceId
        every { resources.obtainTypedArray(testResourceId) } returns mockk()

        val result = context.getIntegerArrayByName(testResourceName)
        assertNotNull(result)
        verify(exactly = 0) { CoreLogger.e(any(), any<Throwable>(), any<String>()) }
        verify(exactly = 0) { CoreLogger.e(any(), any<String>()) }
    }

    @Test
    fun `get integer array returns non null mock integer array`() {
        // GIVEN
        val testResourceName = "test-name"
        every { context.getIntegerArrayByName(testResourceId) } returns mockk()

        val result = context.getIntegerArrayByName(testResourceName)
        assertNotNull(result)
        verify(exactly = 0) { CoreLogger.e(any(), any<Throwable>(), any<String>()) }
        verify(exactly = 0) { CoreLogger.e(any(), any<String>()) }
    }

    @Test
    fun `get integer array returns null and log is invoked`() {
        // GIVEN
        val testResourceName = "test-name"
        every { resources.getIdentifier(testResourceName, "array", any()) } returns testResourceId
        every { resources.obtainTypedArray(testResourceId) } throws Resources.NotFoundException("Not found")

        val result = context.getIntegerArrayByName(testResourceName)
        assertNull(result)
        verify(exactly = 1) {
            CoreLogger.e(
                LogTag.PLAN_RESOURCE_ERROR,
                any(),
                "Plan config resource not found $testResourceName"
            )
        }
    }
}
