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

package me.proton.core.notification.presentation.deeplink

import me.proton.core.notification.test.deeplink.TestDeeplinkIntentProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeeplinkManagerTest {

    private val provider = TestDeeplinkIntentProvider()

    private lateinit var manager: DeeplinkManager

    @Before
    fun before() {
        manager = DeeplinkManager()
    }

    @Test
    fun registerAndReceive1ArgPath() {
        val path = "user/{userId}/action"
        val call = "user/12345678/action"

        manager.register(path) {
            assertEquals(expected = 1, actual = it.args.size)
            assertEquals(expected = "12345678", actual = it.args[0])
            true
        }

        val intent = provider.getActivityIntent(call)
        val handled = manager.handle(intent)
        assertEquals(expected = true, actual = handled)
    }

    @Test
    fun registerAndReceive2ArgsPath() {
        val path = "user/{userId}/action/{actionId}"
        val call = "user/12345678/action/87654321"

        manager.register(path) {
            assertEquals(expected = 2, actual = it.args.size)
            assertEquals(expected = "12345678", actual = it.args[0])
            assertEquals(expected = "87654321", actual = it.args[1])
            true
        }

        val intent = provider.getActivityIntent(call)
        val handled = manager.handle(intent)
        assertEquals(expected = true, actual = handled)
    }

    @Test
    fun registerAndReceive0ArgPath() {
        val path = "user/settings"
        val call = "user/settings"

        manager.register(path) {
            assertEquals(expected = 0, actual = it.args.size)
            true
        }

        val intent = provider.getActivityIntent(call)
        val handled = manager.handle(intent)
        assertEquals(expected = true, actual = handled)
    }

    @Test
    fun registerMultiplePaths() {
        val path1 = "user/{userId}/account"
        val call1 = "user/12345678/account"

        val path2 = "user/settings"
        val call2 = "user/settings"

        manager.register(path1) {
            assertEquals(expected = 1, actual = it.args.size)
            assertEquals(expected = "12345678", actual = it.args[0])
            true
        }

        manager.register(path2) {
            assertEquals(expected = 0, actual = it.args.size)
            false
        }

        val intent1 = provider.getActivityIntent(call1)
        val handled1 = manager.handle(intent1)
        assertEquals(expected = true, actual = handled1)

        val intent2 = provider.getActivityIntent(call2)
        val handled2 = manager.handle(intent2)
        assertEquals(expected = false, actual = handled2)
    }

    @Test
    fun registerDuplicatePaths() {
        val path1 = "user/{userId}/account/show"
        val call1 = "user/12345678/account/show"

        val path2 = "user/{userId}/account/{action}"
        val call2 = "user/87654321/account/show"

        manager.register(path1) {
            assertEquals(expected = 1, actual = it.args.size)
            assertTrue(it.args[0] in listOf("12345678", "87654321"))
            true
        }

        manager.register(path2) {
            assertEquals(expected = 2, actual = it.args.size)
            assertTrue(it.args[0] in listOf("12345678", "87654321"))
            assertEquals(expected = "show", actual = it.args[1])
            false
        }

        val intent1 = provider.getActivityIntent(call1)
        val handled1 = manager.handle(intent1)
        assertEquals(expected = true, actual = handled1)

        val intent2 = provider.getActivityIntent(call2)
        val handled2 = manager.handle(intent2)
        assertEquals(expected = true, actual = handled2)
    }

    @Test
    fun registerDuplicateOverlappingPaths() {
        val path1 = "user/{userId}"
        val call1 = "user/12345678"

        val path2 = "user/{userId}/account/{action}"
        val call2 = "user/87654321/account/show"

        manager.register(path1) {
            assertEquals(expected = 1, actual = it.args.size)
            assertTrue(it.args[0] in listOf("12345678", "87654321"))
            true
        }

        manager.register(path2) {
            assertEquals(expected = 2, actual = it.args.size)
            assertEquals(expected = "87654321", actual = it.args[0])
            assertEquals(expected = "show", actual = it.args[1])
            false
        }

        val intent1 = provider.getActivityIntent(call1)
        val handled1 = manager.handle(intent1)
        assertEquals(expected = true, actual = handled1)

        val intent2 = provider.getActivityIntent(call2)
        val handled2 = manager.handle(intent2)
        assertEquals(expected = true, actual = handled2)
    }

    @Test
    fun registerDuplicateOverlappingNonEqualsPaths() {
        val path1 = "user/{userId}"
        val call1 = "user/12345678"

        val path2 = "user/account/{action}"
        val call2 = "user/account/show"

        manager.register(path1) {
            assertEquals(expected = 1, actual = it.args.size)
            assertTrue(it.args[0] in listOf("12345678", "account"))
            true
        }

        manager.register(path2) {
            assertEquals(expected = 1, actual = it.args.size)
            assertEquals(expected = "show", actual = it.args[0])
            false
        }

        val intent1 = provider.getActivityIntent(call1)
        val handled1 = manager.handle(intent1)
        assertEquals(expected = true, actual = handled1)

        val intent2 = provider.getActivityIntent(call2)
        val handled2 = manager.handle(intent2)
        assertEquals(expected = true, actual = handled2)
    }

    @Test
    fun useGetBroadcastIntent() {
        val path = "user/{userId}/action"
        val call = "user/12345678/action"

        manager.register(path) {
            assertEquals(expected = 1, actual = it.args.size)
            assertEquals(expected = "12345678", actual = it.args[0])
            true
        }

        val intent = provider.getBroadcastIntent(call)
        val handled = manager.handle(intent)
        assertEquals(expected = true, actual = handled)
    }
}
