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
package me.proton.core.humanverification.data.repository

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.data.readFromAssets
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Dino Kadrikj.
 */

const val testDataMostUsedCountries =
    """
        {
  "countries": [
    {
      "country_code": "TOP FIRST",
      "country_en": "Top First",
      "phone_code": 51
    },
    {
      "country_code": "TOP SECOND",
      "country_en": "Top Second",
      "phone_code": 52
    },
    {
      "country_code": "TOP THIRD",
      "country_en": "Top Third",
      "phone_code": 53
    }
   ]
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
      "country_code": "FOURTF",
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
class HumanVerificationLocalRepositoryImplTest {

    private val context = mockk<Context>(relaxed = true)

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.humanverification.data.UtilsKt")
        every { context.readFromAssets(FILE_NAME_ALL_COUNTRIES) } returns testDataCountries
        every { context.readFromAssets(FILE_NAME_MOST_USED_COUNTRIES) } returns testDataMostUsedCountries
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.humanverification.data.UtilsKt")
    }

    @Test
    fun `all countries except most used`() = runBlockingTest {
        val localRepository = HumanVerificationLocalRepositoryImpl(context)

        val result = localRepository.allCountries(false).toList()
        assertEquals(1, result.size)
        val resultList = result[0]
        assertEquals(9, resultList.size)
        val firstCountry = resultList[0]
        assertEquals("FIRST", firstCountry.code)
    }

    @Test
    fun `all countries including most used`() = runBlockingTest {
        val localRepository = HumanVerificationLocalRepositoryImpl(context)

        val result = localRepository.allCountries(true).toList()
        assertEquals(1, result.size)
        val resultList = result[0]
        assertEquals(12, resultList.size)
    }

    @Test
    fun `all countries most used come first`() = runBlockingTest {
        val localRepository = HumanVerificationLocalRepositoryImpl(context)

        val resultList = localRepository.allCountries(true).toList()[0]
        val firstCountry = resultList[0]
        val secondCountry = resultList[1]
        val thirdCountry = resultList[2]
        assertEquals("TOP FIRST", firstCountry.code)
        assertEquals("TOP SECOND", secondCountry.code)
        assertEquals("TOP THIRD", thirdCountry.code)
    }
}
