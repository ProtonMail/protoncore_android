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

package me.proton.core.plan.data.usecase

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.UserManager
import java.lang.IllegalArgumentException
import java.util.Currency
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveUserCurrencyTest : CoroutinesTest by CoroutinesTest() {

    private val savedLocale: Locale = Locale.getDefault()

    private val userId1 = UserId("userId")
    private val userId2 = UserId("another")
    private val userIdAbsent = UserId("absent")

    private val userManager = mockk<UserManager>(relaxed = true) {
        coEvery { this@mockk.observeUser(any()) } answers {
            flowOf(
                when (firstArg<UserId>()) {
                    userId1 -> mockk {
                        every { userId } returns userId1
                        every { currency } returns "CHF"
                    }

                    userId2 -> mockk {
                        every { userId } returns userId2
                        every { currency } returns "EUR"
                    }

                    userIdAbsent -> null
                    else -> null
                }
            )
        }
    }

    private lateinit var tested: ObserveUserCurrencyImpl

    @BeforeTest
    fun setUp() {
        Locale.setDefault(Locale.US)
        tested = ObserveUserCurrencyImpl(userManager)
    }

    @AfterTest
    fun cleanUp() {
        Locale.setDefault(savedLocale)
    }

    @Test
    fun localCurrencyIsUSD() = runTest {
        assertEquals(expected = "USD", actual = tested.localCurrency)
    }

    @Test
    fun localCurrencyIsEUR() = runTest {
        Locale.setDefault(Locale.ITALY)
        val testedNonUsLocale = ObserveUserCurrencyImpl(userManager)
        assertEquals(expected = "EUR", actual = testedNonUsLocale.localCurrency)
    }

    @Test
    fun unknownLocaleNotThrowingException() = runTest {
        mockkStatic(Currency::class)
        every { Currency.getInstance(any() as Locale) } throws IllegalArgumentException("test")
        val testedNonUsLocale = ObserveUserCurrencyImpl(userManager)
        assertNull(testedNonUsLocale.localCurrency)
        assertEquals(expected = "USD", actual = testedNonUsLocale.defaultCurrency)
        unmockkStatic(Currency::class)
    }

    @Test
    fun defaultCurrencyIsUSD() = runTest {
        assertEquals(expected = "USD", actual = tested.defaultCurrency)
    }

    @Test
    fun returnCurrenciesForNoUser() = runTest {
        // Given
        val currency = "USD"
        // When
        tested.invoke(null).test {
            // Then
            assertEquals(expected = currency, actual = awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun returnCurrenciesForUser1() = runTest {
        // Given
        val currency = "CHF"
        // When
        tested.invoke(userId1).test {
            // Then
            assertEquals(expected = currency, actual = awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun returnCurrenciesForUser2() = runTest {
        // Given
        val currency = "EUR"
        // When
        tested.invoke(userId2).test {
            // Then
            assertEquals(expected = currency, actual = awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun returnCurrenciesForAbsent() = runTest {
        // Given
        val currency = "USD"
        // When
        tested.invoke(userIdAbsent).test {
            // Then
            assertEquals(expected = currency, actual = awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
