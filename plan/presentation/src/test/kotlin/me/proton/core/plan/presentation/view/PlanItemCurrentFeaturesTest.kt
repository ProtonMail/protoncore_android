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
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PlanItemCurrentFeaturesTest {

    private val typedArray = mockk<TypedArray>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val resources = mockk<Resources>(relaxed = true)
    private val currentPlanDetails = mockk<PlanDetailsItem.CurrentPlanDetailsItem>(relaxed = true)

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
    fun `test addresses option`() {
        val testString = "%d of #proton_addresses# address"
        every { currentPlanDetails.addresses } returns 1
        every { currentPlanDetails.usedAddresses } returns 1
        every { resources.getQuantityString(any(), 1) } returns testString

        val testType = "#proton_addresses#"
        val result = createCurrentPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = currentPlanDetails
        )
        assertEquals("1 of 1 address", result.first)
        assertEquals(R.drawable.ic_envelope, result.second)
        verify { currentPlanDetails.usedAddresses }
        verify(exactly = 0) { currentPlanDetails.storage }
        verify(exactly = 0) { currentPlanDetails.connections }
        verify(exactly = 0) { currentPlanDetails.domains }
        verify(exactly = 0) { currentPlanDetails.members }
        verify(exactly = 0) { currentPlanDetails.calendars }
    }

    @Test
    fun `test plural addresses option`() {
        val testString = "%d of #proton_addresses# address"
        val testStringPlural = "%d of #proton_addresses# addresses"
        every { currentPlanDetails.addresses } returns 2
        every { currentPlanDetails.usedAddresses } returns 1
        every { resources.getQuantityString(any(), 1) } returns testString
        every { resources.getQuantityString(any(), 2) } returns testStringPlural

        val testType = "#proton_addresses#"
        val result = createCurrentPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = currentPlanDetails
        )
        assertEquals("1 of 2 addresses", result.first)
        assertEquals(R.drawable.ic_envelope, result.second)
        verify { currentPlanDetails.addresses }
        verify(exactly = 0) { currentPlanDetails.storage }
        verify(exactly = 0) { currentPlanDetails.connections }
        verify(exactly = 0) { currentPlanDetails.domains }
        verify(exactly = 0) { currentPlanDetails.members }
        verify(exactly = 0) { currentPlanDetails.calendars }
    }

    @Test
    fun `test vpn option`() {
        every { currentPlanDetails.connections } returns 1
        val testString = "#proton_vpn# connection"
        every { resources.getQuantityString(any(), 1) } returns testString

        val testType = "#proton_vpn#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = currentPlanDetails
        )
        assertEquals("1 connection", result.first)
        assertEquals(R.drawable.ic_shield, result.second)
        verify { currentPlanDetails.connections }
        verify(exactly = 0) { currentPlanDetails.storage }
        verify(exactly = 0) { currentPlanDetails.addresses }
        verify(exactly = 0) { currentPlanDetails.domains }
        verify(exactly = 0) { currentPlanDetails.members }
        verify(exactly = 0) { currentPlanDetails.calendars }
    }

    @Test
    fun `test plural vpn option`() {
        every { currentPlanDetails.connections } returns 2
        val testString = "#proton_vpn# connection"
        val testStringPlural = "#proton_vpn# connections"
        every { resources.getQuantityString(any(), 0) } returns testString
        every { resources.getQuantityString(any(), 1) } returns testString
        every { resources.getQuantityString(any(), 2) } returns testStringPlural

        val testType = "#proton_vpn#"
        val result = createPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = currentPlanDetails
        )
        assertEquals("2 connections", result.first)
        assertEquals(R.drawable.ic_shield, result.second)
        verify { currentPlanDetails.connections }
        verify(exactly = 0) { currentPlanDetails.storage }
        verify(exactly = 0) { currentPlanDetails.addresses }
        verify(exactly = 0) { currentPlanDetails.domains }
        verify(exactly = 0) { currentPlanDetails.members }
        verify(exactly = 0) { currentPlanDetails.calendars }
    }

    @Test
    fun `test domains option`() {
        val testString = "%d of #proton_domains# domain"
        every { currentPlanDetails.domains } returns 1
        every { currentPlanDetails.usedDomains } returns 1
        every { resources.getQuantityString(any(), any()) } returns testString

        val testType = "#proton_domains#"
        val result = createCurrentPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = currentPlanDetails
        )
        assertEquals("1 of 1 domain", result.first)
        assertEquals(R.drawable.ic_globe_language, result.second)
        verify { currentPlanDetails.domains }
        verify(exactly = 0) { currentPlanDetails.storage }
        verify(exactly = 0) { currentPlanDetails.addresses }
        verify(exactly = 0) { currentPlanDetails.connections }
        verify(exactly = 0) { currentPlanDetails.members }
        verify(exactly = 0) { currentPlanDetails.calendars }
    }

    @Test
    fun `test plural domains option`() {
        val testString = "%d of #proton_domains# domain"
        val testStringPlural = "%d of #proton_domains# domains"
        every { currentPlanDetails.domains } returns 2
        every { currentPlanDetails.usedDomains } returns 1
        every { resources.getQuantityString(any(), 0) } returns testString
        every { resources.getQuantityString(any(), 1) } returns testString
        every { resources.getQuantityString(any(), 2) } returns testStringPlural

        val testType = "#proton_domains#"
        val result = createCurrentPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = currentPlanDetails
        )
        assertEquals("1 of 2 domains", result.first)
        assertEquals(R.drawable.ic_globe_language, result.second)
        verify { currentPlanDetails.domains }
        verify(exactly = 0) { currentPlanDetails.storage }
        verify(exactly = 0) { currentPlanDetails.addresses }
        verify(exactly = 0) { currentPlanDetails.connections }
        verify(exactly = 0) { currentPlanDetails.members }
        verify(exactly = 0) { currentPlanDetails.calendars }
    }

    @Test
    fun `test users option`() {
        val testString = "%d of #proton_users# user"
        every { currentPlanDetails.members } returns 1
        every { currentPlanDetails.usedMembers } returns 1
        every { resources.getQuantityString(any(), any()) } returns testString

        val testType = "#proton_users#"
        val result = createCurrentPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = currentPlanDetails
        )
        assertEquals("1 of 1 user", result.first)
        assertEquals(R.drawable.ic_user, result.second)
        verify { currentPlanDetails.members }
        verify(exactly = 0) { currentPlanDetails.storage }
        verify(exactly = 0) { currentPlanDetails.addresses }
        verify(exactly = 0) { currentPlanDetails.connections }
        verify(exactly = 0) { currentPlanDetails.domains }
        verify(exactly = 0) { currentPlanDetails.calendars }
    }

    @Test
    fun `test plural users option`() {
        val testString = "%d of #proton_users# user"
        val testStringPlural = "%d of #proton_users# users"
        every { currentPlanDetails.members } returns 2
        every { currentPlanDetails.usedMembers } returns 1
        every { resources.getQuantityString(any(), 0) } returns testString
        every { resources.getQuantityString(any(), 1) } returns testString
        every { resources.getQuantityString(any(), 2) } returns testStringPlural

        val testType = "#proton_users#"
        val result = createCurrentPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = currentPlanDetails
        )
        assertEquals("1 of 2 users", result.first)
        assertEquals(R.drawable.ic_user, result.second)
        verify { currentPlanDetails.members }
        verify(exactly = 0) { currentPlanDetails.storage }
        verify(exactly = 0) { currentPlanDetails.addresses }
        verify(exactly = 0) { currentPlanDetails.connections }
        verify(exactly = 0) { currentPlanDetails.domains }
        verify(exactly = 0) { currentPlanDetails.calendars }
    }

    @Test
    fun `test calendars option`() {
        val testString = "%d of #proton_calendars# calendar"
        every { currentPlanDetails.calendars } returns 1
        every { currentPlanDetails.usedCalendars } returns 1
        every { resources.getQuantityString(any(), any()) } returns testString

        val testType = "#proton_calendars#"
        val result = createCurrentPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = currentPlanDetails
        )
        assertEquals("1 of 1 calendar", result.first)
        assertEquals(R.drawable.ic_calendar_checkmark, result.second)
        verify { currentPlanDetails.calendars }
        verify(exactly = 0) { currentPlanDetails.storage }
        verify(exactly = 0) { currentPlanDetails.addresses }
        verify(exactly = 0) { currentPlanDetails.connections }
        verify(exactly = 0) { currentPlanDetails.domains }
        verify(exactly = 0) { currentPlanDetails.members }
    }

    @Test
    fun `test plural calendars option`() {
        val testString = "%d of #proton_calendars# calendar"
        val testStringPlural = "%d of #proton_calendars# calendars"
        every { currentPlanDetails.calendars } returns 2
        every { currentPlanDetails.usedCalendars } returns 1
        every { resources.getQuantityString(any(), 0) } returns testString
        every { resources.getQuantityString(any(), 1) } returns testString
        every { resources.getQuantityString(any(), 2) } returns testStringPlural

        val testType = "#proton_calendars#"
        val result = createCurrentPlanFeature(
            type = testType,
            resourceValuesArray = typedArray,
            index = 0,
            context = context,
            plan = currentPlanDetails
        )
        assertEquals("1 of 2 calendars", result.first)
        assertEquals(R.drawable.ic_calendar_checkmark, result.second)
        verify { currentPlanDetails.calendars }
        verify(exactly = 0) { currentPlanDetails.storage }
        verify(exactly = 0) { currentPlanDetails.addresses }
        verify(exactly = 0) { currentPlanDetails.connections }
        verify(exactly = 0) { currentPlanDetails.domains }
        verify(exactly = 0) { currentPlanDetails.members }
    }
}
