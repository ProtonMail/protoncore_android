/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.configuration

import android.os.Bundle
import me.proton.core.configuration.provider.BundleConfigFieldProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BundleConfigFieldProviderTest {

    @Test
    fun getString_returnsCorrectValue() {
        val bundle = Bundle().apply {
            putString("testStringKey", "testStringValue")
        }
        val provider = BundleConfigFieldProvider(bundle)
        assertEquals("testStringValue", provider.getString("testStringKey"))
    }

    @Test
    fun getString_returnsNullForNonexistentKey() {
        val bundle = Bundle()
        val provider = BundleConfigFieldProvider(bundle)
        assertNull(provider.getString("nonexistentKey"))
    }

    @Test
    fun getBoolean_returnsCorrectValue() {
        val bundle = Bundle().apply {
            putBoolean("testBooleanKey", true)
        }
        val provider = BundleConfigFieldProvider(bundle)
        assertTrue(provider.getBoolean("testBooleanKey")!!)
    }

    @Test
    fun getBoolean_returnsNullForNonexistentKey() {
        val bundle = Bundle()
        val provider = BundleConfigFieldProvider(bundle)
        assertNull(provider.getBoolean("nonexistentKey"))
    }

    @Test
    fun getInt_returnsCorrectValue() {
        val bundle = Bundle().apply {
            putInt("testIntKey", 123)
        }
        val provider = BundleConfigFieldProvider(bundle)
        assertEquals(123, provider.getInt("testIntKey"))
    }

    @Test
    fun getInt_returnsNullForNonexistentKey() {
        val bundle = Bundle()
        val provider = BundleConfigFieldProvider(bundle)
        assertNull(provider.getInt("nonexistentKey"))
    }
}
