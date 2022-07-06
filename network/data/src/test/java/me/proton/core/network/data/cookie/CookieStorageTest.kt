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

package me.proton.core.network.data.cookie

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import okhttp3.Cookie
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class MemoryCookieStorageTest : CookieStorageTest<MemoryCookieStorage>() {
    override fun makeCookieStore(): MemoryCookieStorage = MemoryCookieStorage()
}

@RunWith(RobolectricTestRunner::class)
class DiskCookieStorageTest : CookieStorageTest<DiskCookieStorage>() {
    override fun makeCookieStore(): DiskCookieStorage {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return DiskCookieStorage(context, "test-prefs-cookie-store", TestCoroutineScopeProvider)
    }

    override fun cleanup() {
        TestDispatcherProvider.cleanupTestCoroutines()
    }
}

abstract class CookieStorageTest<C : CookieStorage> {
    private lateinit var tested: C

    protected open fun cleanup() {}
    protected abstract fun makeCookieStore(): C

    @BeforeTest
    fun setUp() {
        tested = makeCookieStore()
    }

    @AfterTest
    fun tearDown() {
        cleanup()
    }

    @Test
    fun `empty cookie store`() = runBlockingTest {
        assertTrue(tested.all().toList().isEmpty())
        tested.remove(makeCookie())
    }

    @Test
    fun `store and remove a single cookie`() = runBlockingTest {
        val cookie = makeCookie()
        tested.set(cookie)
        assertContentEquals(listOf(cookie), tested.all().toList())
    }

    @Test
    fun `store multiple cookies`() = runBlockingTest {
        val cookieA = makeCookie(name = "a")
        val cookieB = makeCookie(name = "b")
        tested.set(cookieA)
        tested.set(cookieB)

        assertContentEquals(listOf(cookieA, cookieB), tested.all().toList().sortedBy { it.name })
    }

    @Test
    fun `remove a cookie`() = runBlockingTest {
        val cookieA = makeCookie(name = "a")
        val cookieB = makeCookie(name = "b")
        tested.set(cookieA)
        tested.set(cookieB)
        tested.remove(cookieA)

        assertContentEquals(listOf(cookieB), tested.all().toList())
    }

    @Test
    fun `update a cookie`() = runBlockingTest {
        val cookie = makeCookie()
        tested.set(cookie)

        val updatedCookie = makeCookie(value = "new-value")
        tested.set(updatedCookie)

        assertContentEquals(listOf(updatedCookie), tested.all().toList())
    }

    private fun makeCookie(
        name: String = "test-name",
        value: String = "test-value",
        domain: String = "example.com"
    ): Cookie = Cookie.Builder()
        .name(name)
        .value(value)
        .domain(domain)
        .expiresAt(System.currentTimeMillis())
        .build()
}
