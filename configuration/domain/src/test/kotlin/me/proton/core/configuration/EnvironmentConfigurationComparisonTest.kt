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

package me.proton.core.configuration

import io.mockk.every
import io.mockk.mockk
import me.proton.core.configuration.entity.ConfigContract
import me.proton.core.configuration.entity.ConfigFieldProvider
import me.proton.core.configuration.entity.DefaultConfig
import me.proton.core.configuration.entity.EnvironmentConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KProperty

class EnvironmentConfigurationComparisonTest {

    private lateinit var environmentConfiguration: EnvironmentConfiguration
    private lateinit var defaultEnvironmentConfiguration: DefaultConfig

    val mockConfigData: Map<String, Any?> = mapOf(
        "host" to "testHost",
        "proxyToken" to "testProxyToken",
        "apiPrefix" to "testApiPrefix",
        "baseUrl" to "testBaseUrl",
        "apiHost" to null,
        "hv3Host" to "testHv3Host",
        "hv3Url" to "testHv3Url",
        "useDefaultPins" to true
    )

    @Before
    fun setUp() {
        defaultEnvironmentConfiguration = DefaultConfig(
            host = "testHost",
            proxyToken = "testProxyToken",
            apiPrefix = "testApiPrefix",
            baseUrl = "testBaseUrl",
            apiHost = null,
            hv3Host = "testHv3Host",
            hv3Url = "testHv3Url",
            useDefaultPins = true
        )

        val configFieldProvider = object : ConfigFieldProvider {
            override val configData: Map<String, Any?> = mockConfigData
        }

        environmentConfiguration = EnvironmentConfiguration(configFieldProvider)
    }

    @Test
    fun `should match expected and actual field values`() {
        ConfigContract::class.java.declaredFields.forEach { property ->
            val expectedValue = property.get(defaultEnvironmentConfiguration)
            val actualValue = property.get(environmentConfiguration)
            assertEquals(expectedValue, actualValue)
        }
    }

    @Test
    fun `should return null for non-existent string field`() {
        assertNull(environmentConfiguration.configFieldProvider.stringProvider("nonExistentField"))
    }

    @Test
    fun `should return null for non-existent boolean field`() {
        assertNull(environmentConfiguration.configFieldProvider.booleanProvider("nonExistentField"))
    }

    @Test
    fun `should return correct string value for existing field`() {
        assertEquals("testHost", environmentConfiguration.configFieldProvider.stringProvider("host"))
    }

    @Test
    fun `should return correct boolean value for existing field`() {
        assertEquals(true, environmentConfiguration.configFieldProvider.booleanProvider("useDefaultPins"))
    }


    @Test
    fun `should throw exception for unsupported type in provide function`() {
        val mockProperty = mockk<KProperty<*>>()
        every { mockProperty.name } returns "intField"

        assertThrows(IllegalArgumentException::class.java) {
            environmentConfiguration.provide<Int>(mockProperty).value
        }
    }
}
