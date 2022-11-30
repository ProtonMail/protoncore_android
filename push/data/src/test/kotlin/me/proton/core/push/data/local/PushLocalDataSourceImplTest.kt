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

package me.proton.core.push.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.push.data.local.db.toPush
import me.proton.core.push.data.testing.TestDatabase
import me.proton.core.push.data.testing.allTestPushes
import me.proton.core.push.data.testing.prepare
import me.proton.core.push.data.testing.testAccountEntity
import me.proton.core.push.data.testing.testPush1
import me.proton.core.push.data.testing.testPush2
import me.proton.core.push.data.testing.testPush3
import me.proton.core.push.data.testing.testPushesMessages
import me.proton.core.push.data.testing.testUserEntity
import me.proton.core.push.data.testing.testUserId
import me.proton.core.push.domain.entity.PushObjectType
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class PushLocalDataSourceImplTest {
    private lateinit var testDb: TestDatabase
    private lateinit var tested: PushLocalDataSourceImpl

    @BeforeTest
    fun setUp() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        testDb = Room.inMemoryDatabaseBuilder(appContext, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            .prepare()
        tested = PushLocalDataSourceImpl(testDb)
    }

    @AfterTest
    fun tearDown() {
        testDb.clearAllTables()
        testDb.close()
    }

    @Test
    fun `merge pushes`() = runTest {
        tested.upsertPushes(testPush1)
        assertContentEquals(emptyList(), tested.observeAllPushes(testUserId, PushObjectType.Messages).first())
        assertContentEquals(
            listOf(testPush1),
            testDb.pushDao().observeAllPushes(testUserId, "TestType1").first().map { it.toPush() }
        )
        assertContentEquals(
            emptyList(),
            testDb.pushDao().observeAllPushes(testUserId, "TestType3").first().map { it.toPush() }
        )

        tested.mergePushes(testUserId, testPush2, testPush3)
        assertContentEquals(listOf(testPush2), tested.observeAllPushes(testUserId, PushObjectType.Messages).first())
        assertContentEquals(
            emptyList(),
            testDb.pushDao().observeAllPushes(testUserId, "TestType1").first().map { it.toPush() }
        )
        assertContentEquals(
            listOf(testPush3),
            testDb.pushDao().observeAllPushes(testUserId, "TestType3").first().map { it.toPush() }
        )
    }

    @Test
    fun `merge empty pushes`() = runTest {
        tested.upsertPushes(*allTestPushes.toTypedArray())
        assertContentEquals(listOf(testPush2), tested.observeAllPushes(testUserId, PushObjectType.Messages).first())
        assertContentEquals(
            listOf(testPush1),
            testDb.pushDao().observeAllPushes(testUserId, "TestType1").first().map { it.toPush() }
        )
        assertContentEquals(
            listOf(testPush3),
            testDb.pushDao().observeAllPushes(testUserId, "TestType3").first().map { it.toPush() }
        )

        tested.mergePushes(testUserId, *emptyArray())

        assertContentEquals(emptyList(), tested.observeAllPushes(testUserId, PushObjectType.Messages).first())
        assertContentEquals(
            emptyList(),
            testDb.pushDao().observeAllPushes(testUserId, "TestType1").first().map { it.toPush() }
        )
        assertContentEquals(
            emptyList(),
            testDb.pushDao().observeAllPushes(testUserId, "TestType3").first().map { it.toPush() }
        )
    }

    @Test
    fun `delete all pushes`() = runTest {
        tested.upsertPushes(*testPushesMessages.toTypedArray())
        assertContentEquals(testPushesMessages, tested.observeAllPushes(testUserId, PushObjectType.Messages).first())
        tested.deleteAllPushes()
        assertContentEquals(emptyList(), tested.observeAllPushes(testUserId, PushObjectType.Messages).first())
    }

    @Test
    fun `delete pushes by type`() = runTest {
        tested.upsertPushes(*testPushesMessages.toTypedArray())
        assertContentEquals(testPushesMessages, tested.observeAllPushes(testUserId, PushObjectType.Messages).first())
        tested.deletePushesByType(testUserId, PushObjectType.Messages)
        assertContentEquals(emptyList(), tested.observeAllPushes(testUserId, PushObjectType.Messages).first())
    }

    @Test
    fun `delete pushes by user`() = runTest {
        val otherUserId = UserId("z")
        testDb.accountDao().insertOrUpdate(testAccountEntity(otherUserId))
        testDb.userDao().insertOrUpdate(testUserEntity(otherUserId))

        tested.upsertPushes(testPush2)
        val otherUserPush = testPush2.copy(userId = otherUserId)
        tested.upsertPushes(otherUserPush)

        assertContentEquals(listOf(testPush2), tested.observeAllPushes(testUserId, PushObjectType.Messages).first())
        assertContentEquals(
            listOf(otherUserPush),
            tested.observeAllPushes(otherUserId, PushObjectType.Messages).first()
        )

        tested.deletePushesByUser(testUserId)
        assertContentEquals(emptyList(), tested.observeAllPushes(testUserId, PushObjectType.Messages).first())
        assertContentEquals(
            listOf(otherUserPush),
            tested.observeAllPushes(otherUserId, PushObjectType.Messages).first()
        )
    }

    @Test
    fun `delete push by id`() = runTest {
        tested.upsertPushes(*allTestPushes.toTypedArray())

        tested.observeAllPushes(testUserId, PushObjectType.Messages).test {
            assertTrue(awaitItem().contains(testPush2))
            tested.deletePushesById(testUserId, testPush2.pushId)
            assertFalse(awaitItem().contains(testPush2))
        }
    }
}
