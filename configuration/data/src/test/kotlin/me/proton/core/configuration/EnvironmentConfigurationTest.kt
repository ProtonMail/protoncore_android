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

import org.junit.Assert.assertEquals
import org.junit.Test

class EnvironmentConfigurationTest {

    @Test
    fun `EnvironmentConfiguration initializes correctly with MapFieldProvider`() {
        val configMap = mapOf(
            "host" to "test.proton.me",
            "proxyToken" to "token123",
            "apiPrefix" to "apiTest",
            "useDefaultPins" to false
        )
        val config = EnvironmentConfiguration.fromMap(configMap)

        assertEquals("test.proton.me", config.host)
        assertEquals("token123", config.proxyToken)
        assertEquals("apiTest", config.apiPrefix)
        assertEquals("apiTest.test.proton.me", config.apiHost)
        assertEquals("https://apiTest.test.proton.me", config.baseUrl)
        assertEquals("verify.test.proton.me", config.hv3Host)
        assertEquals("https://verify.test.proton.me", config.hv3Url)
        assertEquals(false, config.useDefaultPins)
    }

    @Test
    fun `EnvironmentConfiguration uses defaults correctly`() {
        val minimalConfigMap = mapOf(
            "host" to "proton.me"
        )
        val config = EnvironmentConfiguration.fromMap(minimalConfigMap)

        assertEquals("proton.me", config.host)
        assertEquals("", config.proxyToken) // Default empty string
        assertEquals("api", config.apiPrefix) // Specified default
        assertEquals("api.proton.me", config.apiHost) // Constructed from defaults
        assertEquals("https://api.proton.me", config.baseUrl) // Constructed URL
        assertEquals("verify.proton.me", config.hv3Host) // Constructed host
        assertEquals("https://verify.proton.me", config.hv3Url) // Constructed URL
        assertEquals(true, config.useDefaultPins) // Default based on `host == "proton.me"`
    }
}
