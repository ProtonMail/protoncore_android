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
import me.proton.core.configuration.entity.ConfigFieldProvider
import me.proton.core.configuration.entity.DefaultConfig
import me.proton.core.configuration.entity.EnvironmentConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KProperty

class EnvironmentConfigurationTest {

    private lateinit var configFieldProvider: ConfigFieldProvider

    @Before
    fun setUp() {
        configFieldProvider = mockk()
    }

    @Test
    fun `should provide String value when property type is String`() {
        val mockProperty = mockk<KProperty<*>>()
        every { mockProperty.name } returns "host"
        every { configFieldProvider.stringProvider("host") } returns "testHost"

        val result: Lazy<String?> = provide(mockProperty)
        assertEquals("testHost", result.value)
    }

    @Test
    fun `should provide Boolean value when property type is Boolean`() {
        val mockProperty = mockk<KProperty<*>>()
        every { mockProperty.name } returns "useDefaultPins"
        every { configFieldProvider.booleanProvider("useDefaultPins") } returns true

        val result: Lazy<Boolean?> = provide(mockProperty)
        assertEquals(true, result.value)
    }

    @Test
    fun `should throw exception for unsupported type`() {
        val mockProperty = mockk<KProperty<*>>()
        every { mockProperty.name } returns "unsupportedField"

        assertThrows(IllegalArgumentException::class.java) {
            val result: Lazy<Int?> = provide(mockProperty)
            result.value
        }
    }

    @Test
    fun `default configuration class`() {
        val expectedConfig = DefaultConfig(
            "host",
            null,
            "test'///12",
            null,
            "apiHost",
            null,
            null,
            false
        )

        val fieldProvider = object : ConfigFieldProvider {
            override val configData = DefaultConfig::class.java.declaredFields.associate {
                it.isAccessible = true
                it.name to it.get(expectedConfig)
            }
        }

        val envConfig = EnvironmentConfiguration(fieldProvider)

        assertEquals(expectedConfig.host, envConfig.host)
        assertEquals(expectedConfig.proxyToken, envConfig.proxyToken)
        assertEquals(expectedConfig.apiPrefix, envConfig.apiPrefix)
        assertEquals(expectedConfig.baseUrl, envConfig.baseUrl)
        assertEquals(expectedConfig.apiHost, envConfig.apiHost)
        assertEquals(expectedConfig.hv3Host, envConfig.hv3Host)
        assertEquals(expectedConfig.hv3Host, envConfig.hv3Host)
        assertEquals(expectedConfig.hv3Url, envConfig.hv3Url)
        assertEquals(expectedConfig.useDefaultPins, envConfig.useDefaultPins)
    }

    private inline fun <reified T> provide(property: KProperty<*>): Lazy<T?> = lazy {
        when (T::class) {
            String::class -> configFieldProvider.stringProvider(property.name) as? T
            Boolean::class -> configFieldProvider.booleanProvider(property.name) as? T
            else -> throw IllegalArgumentException("Unsupported Environment Configuration type")
        }
    }
}
