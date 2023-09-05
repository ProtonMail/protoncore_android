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

package me.proton.configuration.provider

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import me.proton.configuration.provider.data.EmptyMockConfig
import me.proton.configuration.provider.data.MockConfigWithNullValues
import me.proton.configuration.provider.data.MockConfigWithOtherTypes
import me.proton.configuration.provider.data.MockConfigWithUnexpectedValueType
import me.proton.configuration.provider.data.MockEnvironmentConfig
import me.proton.core.configuration.entity.ConfigContract
import me.proton.core.configuration.entity.ConfigFieldProvider
import me.proton.core.configuration.entity.EnvironmentConfiguration
import me.proton.core.configuration.provider.ContentResolverConfigProvider
import me.proton.core.configuration.provider.StaticConfigFieldProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ConfigProviderTest(
    private val provider: ConfigFieldProvider
) {
    @Test
    fun testConfigDataExtractionFromMockClass() {

        assumeTrue(provider is StaticConfigFieldProvider)

        val kClass = Class.forName(MockEnvironmentConfig::class.java.name)
        val instance = kClass.newInstance()

        val expectedData = kClass.declaredFields.associate {
            it.isAccessible = true
            it.name to it.get(instance)
        }

        assertEquals(expectedData, provider.configData)
    }

    @Test
    fun testEnvironmentConfigurationWithStaticProvider() {
        assumeTrue(provider is StaticConfigFieldProvider)

        val expected = MockEnvironmentConfig() as ConfigContract
        val actual = EnvironmentConfiguration(provider) as ConfigContract

        assertEquals(expected.host, actual.host)
        assertEquals(expected.proxyToken, actual.proxyToken)
        assertEquals(expected.apiPrefix, actual.apiPrefix)
        assertEquals(expected.baseUrl, actual.baseUrl)
        assertEquals(expected.apiHost, actual.apiHost)
        assertEquals(expected.hv3Host, actual.hv3Host)
        assertEquals(expected.hv3Host, actual.hv3Host)
        assertEquals(expected.hv3Url, actual.hv3Url)
        assertEquals(expected.useDefaultPins, actual.useDefaultPins)
    }

    @Test
    fun testUnexpectedValueType() {
        assertThrows(IllegalArgumentException::class.java) {
            StaticConfigFieldProvider(MockConfigWithUnexpectedValueType::class.java.name)
        }
    }

    @Test
    fun testClassNotFound() {
        assertThrows(IllegalStateException::class.java) {
            StaticConfigFieldProvider("InvalidClassName")
        }
    }

    @Test
    fun testNullValues() {
        val providerWithNullValues =
            StaticConfigFieldProvider(MockConfigWithNullValues::class.java.name)
        val configData = providerWithNullValues.configData

        assertTrue(configData["nullableString"] == null)
    }

    @Test
    fun testEmptyClass() {
        val providerWithEmptyClass =
            StaticConfigFieldProvider(EmptyMockConfig::class.java.name)
        val configData = providerWithEmptyClass.configData

        assertTrue(configData.isEmpty())
    }

    @Test
    fun testClassWithOtherTypes() {
        assertThrows(IllegalArgumentException::class.java) {
            StaticConfigFieldProvider(MockConfigWithOtherTypes::class.java.name)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private val mockContext = mockk<Context>()
        private val mockContentResolver = mockk<ContentResolver>()
        private val mockUri = mockk<Uri>()

        init {
            mockkStatic(Uri::class)
            every { Uri.parse(any()) } returns mockUri

            every { mockContext.contentResolver } returns mockContentResolver

            val mockCursor = mockk<Cursor>()
            every { mockCursor.getColumnIndex(any()) } returns 0
            every { mockCursor.moveToFirst() } returns true
            every { mockCursor.getString(any()) } returns "mockValue"
            every { mockContentResolver.query(any(), any(), any(), any(), any()) } returns mockCursor
        }

        private val staticConfig =
            StaticConfigFieldProvider(MockEnvironmentConfig::class.java.name)

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(staticConfig),
                arrayOf(
                    ContentResolverConfigProvider(
                        mockContext,
                        defaultConfigProvider = staticConfig
                    )
                )
            )
        }
    }
}
