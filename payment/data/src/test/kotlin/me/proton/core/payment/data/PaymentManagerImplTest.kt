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

package me.proton.core.payment.data

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Role
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaymentManagerImplTest {

    private val currentUserId: UserId = UserId("current")
    private var currentRole: Role = Role.NoOrganization
    private var currentPaymentProviders: Set<PaymentProvider> = emptySet()

    private val userManager: UserManager = mockk {
        coEvery { getUser(any(), any()) } returns mockk {
            every { userId } answers { currentUserId }
            every { role } answers { currentRole }
        }
    }

    private val getAvailablePaymentProviders: GetAvailablePaymentProviders = mockk {
        coEvery { this@mockk.invoke(any<UserId>(), any<Boolean>()) } answers { currentPaymentProviders }
    }

    private lateinit var paymentManager: PaymentManagerImpl

    @BeforeTest
    fun setUp() {
        paymentManager = PaymentManagerImpl(userManager, getAvailablePaymentProviders)
    }

    @Test
    fun `getPaymentProviders just return getAvailablePaymentProviders`() = runTest {
        // GIVEN
        currentPaymentProviders = setOf(PaymentProvider.CardPayment)

        // WHEN
        val actual = paymentManager.getPaymentProviders()

        // THEN
        assertEquals(expected = currentPaymentProviders, actual = actual)
    }

    @Test
    fun `isUpgradeAvailable return true if AvailablePaymentProviders isNotEmpty`() = runTest {
        // GIVEN
        currentPaymentProviders = setOf(PaymentProvider.CardPayment)

        // WHEN
        val actual = paymentManager.isUpgradeAvailable()

        // THEN
        assertTrue(actual)
    }

    @Test
    fun `isUpgradeAvailable return false if AvailablePaymentProviders isEmpty`() = runTest {
        // GIVEN
        currentPaymentProviders = emptySet()

        // WHEN
        val actual = paymentManager.isUpgradeAvailable()

        // THEN
        assertFalse(actual)
    }

    @Test
    fun `isSubscriptionAvailable return true if User Role is NoOrganization`() = runTest {
        // GIVEN
        currentRole = Role.NoOrganization

        // WHEN
        val actual = paymentManager.isSubscriptionAvailable(currentUserId)

        // THEN
        assertTrue(actual)
    }

    @Test
    fun `isSubscriptionAvailable return true if User Role is OrganizationAdmin`() = runTest {
        // GIVEN
        currentRole = Role.OrganizationAdmin

        // WHEN
        val actual = paymentManager.isSubscriptionAvailable(currentUserId)

        // THEN
        assertTrue(actual)
    }

    @Test
    fun `isSubscriptionAvailable return false if User Role is OrganizationMember`() = runTest {
        // GIVEN
        currentRole = Role.OrganizationMember

        // WHEN
        val actual = paymentManager.isSubscriptionAvailable(currentUserId)

        // THEN
        assertFalse(actual)
    }
}
