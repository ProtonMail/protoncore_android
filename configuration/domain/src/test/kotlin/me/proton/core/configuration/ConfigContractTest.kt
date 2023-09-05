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

import me.proton.core.configuration.entity.DefaultConfig
import me.proton.core.configuration.extension.apiPins
import me.proton.core.configuration.extension.certificatePins
import me.proton.core.configuration.extension.mergeWith
import me.proton.core.network.data.di.Constants
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigContractTest {

    private val baseConfig = DefaultConfig(
        host = "baseHost",
        proxyToken = "baseProxyToken",
        apiPrefix = "baseApiPrefix",
        baseUrl = "baseBaseUrl",
        apiHost = "baseApiHost",
        hv3Host = "baseHv3Host",
        hv3Url = "baseHv3Url",
        useDefaultPins = true
    )

    @Test
    fun `should merge two ConfigContracts correctly`() {

        val overrideConfig = DefaultConfig(
            host = "overrideHost",
            proxyToken = null,
            apiPrefix = "overrideApiPrefix",
            baseUrl = null,
            apiHost = "overrideApiHost",
            hv3Host = null,
            hv3Url = "overrideHv3Url",
            useDefaultPins = false
        )

        val mergedConfig = baseConfig.mergeWith(overrideConfig)

        assertEquals("overrideHost", mergedConfig.host)
        assertEquals("baseProxyToken", mergedConfig.proxyToken)
        assertEquals("overrideApiPrefix", mergedConfig.apiPrefix)
        assertEquals("baseBaseUrl", mergedConfig.baseUrl)
        assertEquals("overrideApiHost", mergedConfig.apiHost)
        assertEquals("baseHv3Host", mergedConfig.hv3Host)
        assertEquals("overrideHv3Url", mergedConfig.hv3Url)
        assertEquals(false, mergedConfig.useDefaultPins)
    }

    @Test
    fun `should return correct values for properties`() {
        val configWithoutDefaultPins = DefaultConfig(
            host = "testHost",
            proxyToken = "testProxyToken",
            apiPrefix = "testApiPrefix",
            baseUrl = "testBaseUrl",
            apiHost = "testApiHost",
            hv3Host = "testHv3Host",
            hv3Url = "testHv3Url",
            useDefaultPins = false
        )

        assertArrayEquals(Constants.DEFAULT_SPKI_PINS, baseConfig.certificatePins)
        assertArrayEquals(emptyArray<String>(), configWithoutDefaultPins.certificatePins)

        assertEquals(Constants.ALTERNATIVE_API_SPKI_PINS, baseConfig.apiPins)
        assertEquals(emptyList<String>(), configWithoutDefaultPins.apiPins)
    }
}
