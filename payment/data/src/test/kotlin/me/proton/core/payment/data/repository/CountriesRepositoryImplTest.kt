/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.payment.data.repository

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.serialization.SerializationException
import me.proton.core.data.assets.readFromAssets
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CountriesRepositoryImplTest {

    // region mock data
    private val context = mockk<Context>(relaxed = true)
    // endregion

    // region test data
    private val testDataCountries =
        """
            {
              "countries": [
                {
                  "country_code": "FIRST",
                  "country_en": "First",
                  "phone_code": 1
                },
                {
                  "country_code": "SECOND",
                  "country_en": "Second",
                  "phone_code": 2
                },
                {
                  "country_code": "THIRD",
                  "country_en": "Third",
                  "phone_code": 3
                }
               ]
            }
    """

    // endregion
    private lateinit var repository: CountriesRepositoryImpl

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.data.assets.UtilsKt")
        every { context.readFromAssets("country_codes.json") } returns testDataCountries
        repository = CountriesRepositoryImpl(context)
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.data.assets.UtilsKt")
    }

    @Test
    fun `countries return success non empty list`() {
        // WHEN
        val result = repository.getCountries()
        // THEN
        assertEquals(3, result.size)
        assertEquals("First", result[0].name)
    }

    @Test
    fun `countries return success empty list`() {
        // GIVEN
        val emptyCountries = """
            {
                "countries": []
            }
        """
        every { context.readFromAssets("country_codes.json") } returns emptyCountries
        // WHEN
        val result = repository.getCountries()
        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun `countries return error`() {
        // GIVEN
        val errorCountries = """
            
        """
        every { context.readFromAssets("country_codes.json") } returns errorCountries
        // THEN
        assertFailsWith(SerializationException::class) {
            repository.getCountries()
        }
    }

    @Test
    fun `country code returns success`() {
        val result = repository.getCountryCodeByName("Second")
        assertEquals("SECOND", result)
    }

    @Test
    fun `country code for nonexistent returns properly`() {
        val result = repository.getCountryCodeByName("Fourth")
        assertEquals("Fourth", result)
    }
}
