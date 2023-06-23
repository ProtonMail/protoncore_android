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

package me.proton.core.notification.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.notification.data.local.db.NotificationDao
import me.proton.core.notification.data.local.db.NotificationDatabase
import me.proton.core.notification.data.local.db.NotificationEntity
import me.proton.core.notification.data.repository.testing.TestDatabase
import me.proton.core.notification.data.repository.testing.allTestNotifications
import me.proton.core.notification.data.repository.testing.prepare
import me.proton.core.notification.data.repository.testing.testNotification1
import me.proton.core.notification.data.repository.testing.testNotification2
import me.proton.core.notification.data.repository.testing.testNotification3
import me.proton.core.notification.data.repository.testing.testUserId
import me.proton.core.notification.domain.entity.NotificationId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
internal class NotificationLocalDataSourceImplTest {

    private val dao = mockk<NotificationDao>(relaxUnitFun = true)
    private val mockedDatabase = mockk<NotificationDatabase> {
        every { notificationDao() } returns dao
    }
    private val mockedLocalDataSource = NotificationLocalDataSourceImpl(mockedDatabase)

    private lateinit var testDb: TestDatabase
    private lateinit var tested: NotificationLocalDataSourceImpl

    @Before
    fun beforeEveryTest() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        testDb = Room.inMemoryDatabaseBuilder(appContext, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            .prepare()
        tested = NotificationLocalDataSourceImpl(testDb)
    }

    @After
    fun afterEveryTest() {
        testDb.clearAllTables()
        testDb.close()
    }

    @Test
    fun `upsert notifications observe test`() = runTest {
        tested.upsertNotifications(*allTestNotifications.toTypedArray())
        val result = tested.observeAllNotificationsByUser(testUserId).first()
        assertContentEquals(allTestNotifications, result)
    }

    @Test
    fun `upsert notifications get test`() = runTest {
        tested.upsertNotifications(*allTestNotifications.toTypedArray())
        val result = tested.getNotificationsByUser(testUserId)
        assertContentEquals(allTestNotifications, result)
    }

    @Test
    fun `delete notifications test`() = runTest {
        tested.upsertNotifications(*allTestNotifications.toTypedArray())
        val result = tested.getNotificationsByUser(testUserId)
        assertContentEquals(allTestNotifications, result)
        tested.deleteNotificationsById(testUserId, testNotification1.notificationId)
        val resultAfterDelete = tested.getNotificationsByUser(testUserId)
        assertContentEquals(listOf(testNotification2, testNotification3), resultAfterDelete)
    }

    @Test
    fun `delete all notifications test`() = runTest {
        tested.upsertNotifications(*allTestNotifications.toTypedArray())
        val result = tested.getNotificationsByUser(testUserId)
        assertContentEquals(allTestNotifications, result)
        tested.deleteAllNotificationsByUser(testUserId)
        val resultAfterDelete = tested.getNotificationsByUser(testUserId)
        assertContentEquals(emptyList(), resultAfterDelete)
    }

    @Test
    fun `get notification by id test`() = runTest {
        tested.upsertNotifications(*allTestNotifications.toTypedArray())
        val result = tested.getNotificationsByUser(testUserId)
        assertContentEquals(allTestNotifications, result)
        val resultNotification = tested.getNotificationById(testUserId, testNotification3.notificationId)
        assertEquals(testNotification3, resultNotification)
    }

    @Test
    fun `get notification dao verification`() = runTest {
        coEvery { dao.getNotification(testUserId, testNotification1.notificationId) } returns NotificationEntity(
            NotificationId("1"),
            testUserId,
            time = 1,
            type = "TestType1",
            payload = "{}"
        )
        val result = mockedLocalDataSource.getNotificationById(testUserId, testNotification1.notificationId)
        assertEquals(NotificationId("1"), result?.notificationId)
        coVerify { dao.getNotification(testUserId, testNotification1.notificationId) }
    }

    @Test
    fun `get all notifications dao verification`() = runTest {
        coEvery { dao.getAllNotifications(testUserId) } returns listOf(
            NotificationEntity(
                NotificationId("1"),
                testUserId,
                time = 1,
                type = "TestType1",
                payload = "{}"
            )
        )
        val result = mockedLocalDataSource.getNotificationsByUser(testUserId)
        assertEquals(1, result.size)
        assertEquals(NotificationId("1"), result[0].notificationId)
        coVerify { dao.getAllNotifications(testUserId) }
    }

    @Test
    fun `observe all notifications dao verification`() = runTest {
        every { dao.observeAllNotifications(testUserId) } returns flowOf(
            listOf(
                NotificationEntity(
                    NotificationId("1"),
                    testUserId,
                    time = 1,
                    type = "TestType1",
                    payload = "{}"
                )
            )
        )
        val result = mockedLocalDataSource.observeAllNotificationsByUser(testUserId).first()
        assertEquals(1, result.size)
        assertEquals(NotificationId("1"), result[0].notificationId)
        coVerify { dao.observeAllNotifications(testUserId) }
    }

    @Test
    fun `delete all notifications dao verification`() = runTest {
        coEvery { dao.deleteNotifications(userIds = *listOf(testUserId).toTypedArray()) } returns Unit
        mockedLocalDataSource.deleteAllNotificationsByUser(testUserId)
        coVerify { dao.deleteNotifications(*listOf(testUserId).toTypedArray()) }
    }

    @Test
    fun `delete all notifications with ids dao verification`() = runTest {
        val notificationIdsSlot = mutableListOf<NotificationId?>()
        coEvery {
            dao.deleteNotifications(
                userId = testUserId,
                notificationIds = *varargAllNullable { notificationIdsSlot.add(it) }
            )
        } returns Unit
        val notificationIdsToDelete = listOf(testNotification2.notificationId, testNotification3.notificationId)
        mockedLocalDataSource.deleteNotificationsById(testUserId, *notificationIdsToDelete.toTypedArray())
        assertEquals(2, notificationIdsSlot.size)
        assertEquals("2", notificationIdsSlot[0]!!.id)
        assertEquals("3", notificationIdsSlot[1]!!.id)
    }
}