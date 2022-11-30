/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.util.android.sharedpreferences

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/*
 * Runs with Robolectric as we can't reliably mock listeners in a easy way
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ObserveTest : CoroutinesTest by UnconfinedCoroutinesTest() {
    private val prefs = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("test", Context.MODE_PRIVATE)

    @After
    fun tearDown() {
        prefs.clearAll()
    }

    @Test
    fun `observe all emits initial value`() = coroutinesTest {

        // given
        prefs["string"] = "hello"
        prefs["int"] = 10

        val expected = mapOf(
            "string" to "hello",
            "int" to 10
        )

        // when
        val flow = prefs.observe()

        // then
        assertEquals(expected, flow.first().sharedPreferences.all)
    }

    @Test
    fun `observe all emits every change`() = coroutinesTest {

        // given
        val flow = prefs.observe()
        val result = mutableListOf<Pair<String?, Map<String, Any?>>>()

        val expected = listOf(
            null to emptyMap(),
            "string" to mapOf(
                "string" to "hello"
            ),
            "int" to mapOf(
                "string" to "hello",
                "int" to 10,
            ),
            "boolean" to mapOf(
                "boolean" to true,
                "string" to "hello",
                "int" to 10,
            )
        )

        // when
        val job = launch {
            flow.map { it.key to it.sharedPreferences.all.toMap() }.take(4).toList(result)
        }
        prefs["string"] = "hello"
        prefs["int"] = 10
        prefs["boolean"] = true

        // then
        assertEquals<List<Pair<String?, Map<String, Any?>>>>(expected, result)
        job.cancel()
    }

    @Test
    fun `observe single emits initial value`() = coroutinesTest {

        // given
        prefs["string"] = "hello"

        // when
        val flow = prefs.observe<String>(key = "string")

        // then
        assertEquals("hello", flow.first())
    }

    @Test
    fun `observe single emits every change`() = coroutinesTest {

        // given
        val flow = prefs.observe<String>(key = "string")
        val result = mutableListOf<String?>()

        val expected = listOf(
            null,
            "hello",
            "hi"
        )

        // when
        val job = launch {
            flow.take(3).toList(result)
        }
        prefs["string"] = "hello"
        prefs["string"] = "hi"

        // then
        assertEquals(expected, result)
        job.cancel()
    }

    @Test
    fun `observe single does not emit for unrelated changes`() = coroutinesTest {

        // given
        val flow = prefs.observe<String>(key = "string")
        val result = mutableListOf<String?>()

        val expected = listOf(
            null,
            "hello",
        )

        // when
        val job = launch {
            flow.take(2).toList(result)
        }
        prefs["other"] = "hi"
        prefs["string"] = "hello"

        // then
        assertEquals(expected, result)
        job.cancel()
    }

}
