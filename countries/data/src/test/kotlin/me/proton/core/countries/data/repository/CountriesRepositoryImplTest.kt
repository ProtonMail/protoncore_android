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

package me.proton.core.countries.data.repository

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.serialization.SerializationException
import me.proton.core.data.assets.readFromAssets
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

const val testDataEmptyCountries =
    """
        {
            "countries": []
        }
"""

const val testDataCountries =
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
    },
    {
      "country_code": "FOURTH",
      "country_en": "Fourth",
      "phone_code": 4
    },
    {
      "country_code": "FIFTH",
      "country_en": "Fifth",
      "phone_code": 5
    },
    {
      "country_code": "SIXT",
      "country_en": "Sixt",
      "phone_code": 6
    },
    {
      "country_code": "SEVENTH",
      "country_en": "Seventh",
      "phone_code": 7
    },
    {
      "country_code": "EIGHT",
      "country_en": "Eight",
      "phone_code": 8
    },
    {
      "country_code": "NINTH",
      "country_en": "Ninth",
      "phone_code": 9
    }
   ]
}
    """

@InternalCoroutinesApi
class CountriesRepositoryImplTest {

    private val context = mockk<Context>(relaxed = true)
    private lateinit var repository: CountriesRepositoryImpl

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.data.asset.AssetReaderKt")
        every { context.readFromAssets(FILE_NAME_ALL_COUNTRIES) } returns testDataCountries
        repository = CountriesRepositoryImpl(context)
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.data.asset.AssetReaderKt")
    }

    @Test
    fun `all countries returns sorted list`() = runBlockingTest {
        val result = repository.getAllCountriesSorted()
        assertEquals(9, result.size)
        val firstCountry = result[0]
        assertEquals("EIGHT", firstCountry.code)
    }

    @Test
    fun `all countries returns empty`() = runBlockingTest {
        every { context.readFromAssets(FILE_NAME_ALL_COUNTRIES) } returns testDataEmptyCountries
        val result = repository.getAllCountriesSorted()
        assertEquals(0, result.size)
    }

    @Test
    fun `countries return success for empty file country list`() {
        // GIVEN
        val emptyCountries = """
            {
                "countries": []
            }
        """
        every { context.readFromAssets(FILE_NAME_ALL_COUNTRIES) } returns emptyCountries
        // WHEN
        val result = repository.getAllCountriesSorted()
        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun `countries return error for invalid empty file`() {
        // GIVEN
        val errorCountries = """
            
        """
        every { context.readFromAssets(FILE_NAME_ALL_COUNTRIES) } returns errorCountries
        // THEN
        assertFailsWith(SerializationException::class) {
            repository.getAllCountriesSorted()
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
        assertEquals("FOURTH", result)
    }
}
