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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import me.proton.core.configuration.entity.ConfigContract
import org.junit.Test

class ContentResolverConfigManagerTest {
    @Test
    fun queryAtClassPath_ReturnsNonNullMap() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val manager = ContentResolverConfigManager(appContext)

        val result = manager.queryAtClassPath(MyConfigClass::class)

        assertNotNull(result)
        assertTrue(result!!.isNotEmpty())
    }

    @Test
    fun insertConfigFieldMapAtClassPath_InsertsDataCorrectly() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val manager = ContentResolverConfigManager(appContext)

        val testMap = mapOf("testKey" to "testValue")
        val insertUri = manager.insertConfigFieldMapAtClassPath(testMap, MyConfigClass::class)

        assertNotNull(insertUri)

        val queryResult = manager.queryAtClassPath(MyConfigClass::class)
        assertTrue("testValue" == queryResult?.get("testKey"))
    }

    @Test
    fun queryAtClassPathWithInternalClassReturnsNull() {
        class Internal {
            val host = "test"
        }

        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val manager = ContentResolverConfigManager(appContext)

        val result = manager.queryAtClassPath(Internal::class)

        assertNull(result)
    }
}

class MyConfigClass: ConfigContract {
    override val host: String
        get() = "host"
    override val proxyToken: String
        get() = "proxyToken"
    override val apiPrefix: String
        get() = "apiPrefix"
    override val apiHost: String
        get() = "apiHost"
    override val baseUrl: String
        get() = "baseUrl"
    override val hv3Host: String
        get() = "hv3Host"
    override val hv3Url: String
        get() = "hv3Url"
    override val useDefaultPins: Boolean
        get() = false
}
