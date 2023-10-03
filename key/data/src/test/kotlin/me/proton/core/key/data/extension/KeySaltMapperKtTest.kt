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

package me.proton.core.key.data.extension

import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.entity.KeySaltEntity
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKeySalt
import me.proton.core.test.kotlin.assertIs
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class KeySaltMapperKtTest {

    // region test data
    private val testUserId = UserId("test-user-id")
    // endregion

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.key.data.extension.KeySaltMapperKt")
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.key.data.extension.KeySaltMapperKt")
    }

    @Test
    fun `keySaltEntity to PrivateKeySalt`() {
        val keySaltEntity = KeySaltEntity(testUserId, KeyId("test-key-id"), "test-key-salt")
        val result = keySaltEntity.toPrivateKeySalt()
        assertIs<PrivateKeySalt>(result)
        assertEquals(result.keySalt, "test-key-salt")
        assertEquals(result.keyId, KeyId("test-key-id"))
    }

    @Test
    fun `keySaltEntity list to PrivateKeySalt`() {
        val keySaltEntity = KeySaltEntity(testUserId, KeyId("test-key-id"), "test-key-salt")
        val keySaltEntityList = listOf(keySaltEntity)
        val result = keySaltEntityList.toPrivateKeySaltList()
        assertIs<List<PrivateKeySalt>>(result)
        assertEquals(1, result.size)
        verify { keySaltEntity.toPrivateKeySalt() }
        val privateKeySaltResult = result[0]
        assertEquals(privateKeySaltResult.keySalt, "test-key-salt")
        assertEquals(privateKeySaltResult.keyId, KeyId("test-key-id"))
    }

    @Test
    fun `keySaltEntity list to PrivateKeySalt2`() {
        val keySaltEntityList = emptyList<KeySaltEntity>()
        val result = keySaltEntityList.toPrivateKeySaltList()
        assertIs<List<PrivateKeySalt>>(result)
        assertEquals(0, result.size)
    }

}