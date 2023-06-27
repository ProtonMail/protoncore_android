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

package me.proton.core.notification.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import me.proton.core.notification.data.local.NotificationLocalDataSourceImpl
import me.proton.core.notification.data.repository.testing.TestDatabase
import me.proton.core.notification.data.repository.testing.prepare
import me.proton.core.notification.data.repository.testing.testNotification1
import me.proton.core.notification.data.repository.testing.testNotification2
import me.proton.core.notification.data.repository.testing.testNotification3
import me.proton.core.notification.data.repository.testing.testUserId
import me.proton.core.notification.domain.repository.NotificationLocalDataSource
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
internal class NotificationRepositoryImplTest : CoroutinesTest by UnconfinedCoroutinesTest() {
    private lateinit var localDataSource: NotificationLocalDataSource
    private lateinit var tested: NotificationRepositoryImpl
    private lateinit var testDb: TestDatabase

    @Before
    fun beforeEveryTest() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        testDb = Room.inMemoryDatabaseBuilder(appContext, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            .prepare()
        localDataSource = NotificationLocalDataSourceImpl(testDb)
        tested =
            NotificationRepositoryImpl(localDataSource, TestCoroutineScopeProvider(dispatchers))
    }

    @After
    fun tearDown() {
        testDb.clearAllTables()
        testDb.close()
    }

    @Test
    fun `get all notifications`() = runTest {
        localDataSource.upsertNotifications(testNotification1, testNotification2)
        val result = tested.getAllNotificationsByUser(testUserId)
        assertContentEquals(listOf(testNotification1, testNotification2), result)
        val resultLocal = tested.getAllNotificationsByUser(testUserId)
        assertContentEquals(listOf(testNotification1, testNotification2), resultLocal)
    }

    @Test
    fun `get all notifications empty`() = runTest {
        localDataSource.upsertNotifications(testNotification1, testNotification2)
        val result = tested.getAllNotificationsByUser(testUserId)
        assertContentEquals(listOf(testNotification1, testNotification2), result)
        val resultLocal = tested.getAllNotificationsByUser(testUserId)
        assertContentEquals(listOf(testNotification1, testNotification2), resultLocal)
    }

    @Test
    fun `get notifications by id`() = runTest {
        localDataSource.upsertNotifications(testNotification1, testNotification2)
        val result = tested.getNotificationById(testUserId, testNotification1.notificationId)
        assertEquals(testNotification1, result)
        val resultLocal = tested.getNotificationById(testUserId, testNotification1.notificationId)
        assertEquals(testNotification1, resultLocal)
    }

    @Test
    fun `observe all notifications`() = runTest {
        localDataSource.upsertNotifications(testNotification1, testNotification2)
        tested.observeAllNotificationsByUser(testUserId).test {
            assertContentEquals(listOf(testNotification1, testNotification2), awaitItem())
            tested.deleteNotificationById(testUserId, testNotification2.notificationId)
            assertContentEquals(listOf(testNotification1), awaitItem())
        }
    }

    @Test
    fun `delete all notifications`() = runTest {
        localDataSource.upsertNotifications(testNotification1, testNotification2)
        tested.observeAllNotificationsByUser(testUserId).test {
            assertContentEquals(listOf(testNotification1, testNotification2), awaitItem())
            tested.deleteAllNotificationsByUser(testUserId)
            assertContentEquals(emptyList(), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `upsert notifications`() = runTest {
        localDataSource.upsertNotifications(testNotification1, testNotification2)
        tested.observeAllNotificationsByUser(testUserId).test {
            assertContentEquals(listOf(testNotification1, testNotification2), awaitItem())
            tested.upsertNotifications(testNotification3)
            assertContentEquals(listOf(testNotification1, testNotification2, testNotification3), awaitItem())
            expectNoEvents()
        }
    }
}