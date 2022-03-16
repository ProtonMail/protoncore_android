/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.plan.presentation.view

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PlanItemFeaturesKtTest {

    private val typedArray = mockk<TypedArray>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val resources = mockk<Resources>(relaxed = true)
    private val mockedPlan = mockk<PlanDetailsItem.PaidPlanDetailsItem>(relaxed = true)

    private val testResourceId = 1

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.plan.presentation.view.PlanItemFeaturesKt")
        every { context.resources } returns resources
        every { typedArray.getResourceId(any(), 0) } returns testResourceId
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.plan.presentation.view.PlanItemFeaturesKt")
    }

    @Test
    fun `test storage option`() {
        val testString = "test-string-#proton_storage#"
        every { resources.getQuantityString(any(), any()) } returns testString

        val testType = "#proton_storage#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-0 B", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.members }
        verify(exactly = 0) { mockedPlan.calendars }
    }

    @Test
    fun `test addresses option`() {
        val testString = "test-string-#proton_addresses#"
        every { resources.getQuantityString(any(), 0) } returns testString

        val testType = "#proton_addresses#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-0", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.members }
        verify(exactly = 0) { mockedPlan.calendars }
    }

    @Test
    fun `test plural addresses option`() {
        val testString = "test-string-#proton_addresses#"
        val testStringPlural = "test-string-plural-#proton_addresses#"
        every { resources.getQuantityString(any(), 0) } returns testString
        every { resources.getQuantityString(any(), 2) } returns testStringPlural
        every { mockedPlan.addresses } returns 2

        val testType = "#proton_addresses#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-plural-2", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.members }
        verify(exactly = 0) { mockedPlan.calendars }
    }

    @Test
    fun `test vpn option`() {
        val testString = "test-string-#proton_vpn#"
        every { resources.getQuantityString(any(), any()) } returns testString

        val testType = "#proton_vpn#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-0", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.members }
        verify(exactly = 0) { mockedPlan.calendars }
    }

    @Test
    fun `test plural vpn option`() {
        val testString = "test-string-#proton_vpn#"
        val testStringPlural = "test-string-plural-#proton_vpn#"
        every { resources.getQuantityString(any(), 0) } returns testString
        every { resources.getQuantityString(any(), 2) } returns testStringPlural
        every { mockedPlan.connections } returns 2

        val testType = "#proton_vpn#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-plural-2", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.members }
        verify(exactly = 0) { mockedPlan.calendars }
    }

    @Test
    fun `test domains option`() {
        val testString = "test-string-#proton_domains#"
        every { resources.getQuantityString(any(), any()) } returns testString

        val testType = "#proton_domains#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-0", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.members }
        verify(exactly = 0) { mockedPlan.calendars }
    }

    @Test
    fun `test plural domains option`() {
        val testString = "test-string-#proton_domains#"
        val testStringPlural = "test-string-plural-#proton_domains#"
        every { resources.getQuantityString(any(), 0) } returns testString
        every { resources.getQuantityString(any(), 2) } returns testStringPlural
        every { mockedPlan.domains } returns 2

        val testType = "#proton_domains#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-plural-2", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.members }
        verify(exactly = 0) { mockedPlan.calendars }
    }

    @Test
    fun `test users option`() {
        val testString = "test-string-#proton_users#"
        every { resources.getQuantityString(any(), any()) } returns testString

        val testType = "#proton_users#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-0", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.members }
        verify(exactly = 0) { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.calendars }
    }

    @Test
    fun `test plural users option`() {
        val testString = "test-string-#proton_users#"
        val testStringPlural = "test-string-plural-#proton_users#"
        every { resources.getQuantityString(any(), 0) } returns testString
        every { resources.getQuantityString(any(), 2) } returns testStringPlural
        every { mockedPlan.members } returns 2

        val testType = "#proton_users#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-plural-2", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.members }
        verify(exactly = 0) { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.calendars }
    }

    @Test
    fun `test calendars option`() {
        val testString = "test-string-#proton_calendars#"
        every { resources.getQuantityString(any(), any()) } returns testString

        val testType = "#proton_calendars#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-0", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.calendars }
        verify(exactly = 0) { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.members }
    }

    @Test
    fun `test plural calendars option`() {
        val testString = "test-string-#proton_calendars#"
        val testStringPlural = "test-string-plural-#proton_calendars#"
        every { resources.getQuantityString(any(), 0) } returns testString
        every { resources.getQuantityString(any(), 2) } returns testStringPlural
        every { mockedPlan.calendars } returns 2

        val testType = "#proton_calendars#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = mockedPlan
        )
        assertEquals("test-string-plural-2", result.first)
        assertEquals(R.drawable.ic_baseline_check, result.second)
        verify { mockedPlan.calendars }
        verify(exactly = 0) { mockedPlan.storage }
        verify(exactly = 0) { mockedPlan.addresses }
        verify(exactly = 0) { mockedPlan.connections }
        verify(exactly = 0) { mockedPlan.domains }
        verify(exactly = 0) { mockedPlan.members }
    }
}
