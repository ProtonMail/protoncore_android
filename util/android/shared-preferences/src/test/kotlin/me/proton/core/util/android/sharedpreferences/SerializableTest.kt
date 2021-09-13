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

import me.proton.core.test.android.mocks.mockSharedPreferences
import me.proton.core.util.android.sharedpreferences.internal.SerializableTestChild
import me.proton.core.util.android.sharedpreferences.internal.SerializableTestClass
import me.proton.core.util.kotlin.startsWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * Test suite for serializable items within SharedPreferences
 * @author Davide Farella
 */
internal class SerializableTest {

    private val p = mockSharedPreferences

    @Test
    fun `Preferences can store and get serializable item`() {

        // GIVEN
        val s = SerializableTestClass(
            "test",
            SerializableTestChild(
                15,
                true
            )
        )

        // WHEN
        p["key1"] = s

        // THEN
        assertEquals(s, p.get<SerializableTestClass>("key1"))
    }

    private class NonSerializableTestClass

    @Test
    fun `proper message is displayed if given class is not serializable`() {

        // GIVEN
        val ns = NonSerializableTestClass()

        // WHEN
        val block = { p["key2"] = ns }

        // THEN
        val message: String? = assertFails(block).localizedMessage
        assert(message?.startsWith("Serializer for class 'NonSerializableTestClass' is not found.") == true) {
            message.toString()
        }
    }
}
