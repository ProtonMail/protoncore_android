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

package me.proton.core.test.android.mocks

import android.content.SharedPreferences
import io.mockk.MockKAnswerScope
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk

/**
 * Mock of [SharedPreferences]
 * @author Davide Farella
 */
val mockSharedPreferences = newMockSharedPreferences

/** @return every time a new instance */
val newMockSharedPreferences
    get() = mockk<SharedPreferences> {

        val m = mutableMapOf<String, Any?>()

        @Suppress("UNCHECKED_CAST")
        fun <T, B> MockKAnswerScope<T, B>.getMap() = m.getOrElse(firstArg()) { secondArg() } as B

        val editor = spyk(MockSharedPreferencesEditor(m))

        every { contains(any()) } answers { m.containsKey(firstArg()) }
        every { edit() } returns editor

        every { all } answers { m }
        every { getBoolean(any(), any()) } answers { getMap() }
        every { getFloat(any(), any()) } answers { getMap() }
        every { getInt(any(), any()) } answers { getMap() }
        every { getLong(any(), any()) } answers { getMap() }
        every { getString(any(), any()) } answers { getMap() }
        every { getStringSet(any(), any()) } answers { getMap() }
    }

private class MockSharedPreferencesEditor(
    private val m: MutableMap<String, Any?>
) : SharedPreferences.Editor {

    override fun putBoolean(key: String, value: Boolean) = apply { m[key] = value }
    override fun putFloat(key: String, value: Float) = apply { m[key] = value }
    override fun putInt(key: String, value: Int) = apply { m[key] = value }
    override fun putLong(key: String, value: Long) = apply { m[key] = value }
    override fun putString(key: String, value: String?) = apply { m[key] = value }
    override fun putStringSet(key: String, values: MutableSet<String>?) = apply { m[key] = values }
    override fun remove(key: String) = apply { m -= key }
    override fun clear() = apply { m.clear() }
    override fun apply() { /* noop */ }
    override fun commit() = true
}
