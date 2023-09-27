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

import me.proton.core.configuration.extension.configContractFields
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.reflect.KFunction1

class EnvironmentConfigurationTest {

    private val mockConfigData: Map<String, Any?> = mapOf(
        "host" to "testHost",
        "proxyToken" to "testProxyToken",
        "apiPrefix" to "testApiPrefix",
        "baseUrl" to "testBaseUrl",
        "apiHost" to "api.host",
        "hv3Host" to "testHv3Host",
        "hv3Url" to "testHv3Url"
    )

    @Suppress("UNCHECKED_CAST")
    private val expected = EnvironmentConfiguration(mockConfigData::get as KFunction1<String, String?>)

    private class ValidStaticConfig {
        val host: String = "testHost"
        val proxyToken: String = "testProxyToken"
        val apiPrefix: String = "testApiPrefix"
        val baseUrl: String = "testBaseUrl"
        val apiHost: String = "api.host"
        val hv3Host: String = "testHv3Host"
        val hv3Url: String = "testHv3Url"
    }

    private class InvalidStaticConfig {
        val host = 0
    }

    @Test
    fun `load config from map`() {
        val actual = EnvironmentConfiguration.fromMap(mockConfigData)
        assertEquals(
            actual.configContractFields,
            expected.configContractFields
        )
    }

    @Test
    fun `throw error for unsupported type when loading from map`() {
        assertThrows(IllegalArgumentException::class.java) {
            EnvironmentConfiguration.fromMap(mapOf("host" to arrayOf("")))
        }
    }

    @Test
    fun `throw error for loading non-existent class`() {
        assertThrows(IllegalStateException::class.java) {
            EnvironmentConfiguration.fromClass("null")
        }
    }

    @Test
    fun `throw error for loading invalid config`() {
        assertThrows(IllegalArgumentException::class.java) {
            EnvironmentConfiguration.fromClass(InvalidStaticConfig::class.java.name)
        }
    }

    @Test
    fun `load config from class`() {
        val actual = EnvironmentConfiguration.fromClass(ValidStaticConfig::class.java.name)
        assertEquals(actual.configContractFields, expected.configContractFields)
    }
}
