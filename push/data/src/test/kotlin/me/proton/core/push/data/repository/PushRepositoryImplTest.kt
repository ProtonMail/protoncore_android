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

package me.proton.core.push.data.repository

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import androidx.work.WorkRequest
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.proton.core.push.data.local.PushLocalDataSourceImpl
import me.proton.core.push.data.testing.TestDatabase
import me.proton.core.push.data.testing.allTestPushes
import me.proton.core.push.data.testing.prepare
import me.proton.core.push.data.testing.testPush1
import me.proton.core.push.data.testing.testPush2
import me.proton.core.push.data.testing.testPushesMessages
import me.proton.core.push.data.testing.testUserId
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.local.PushLocalDataSource
import me.proton.core.push.domain.remote.PushRemoteDataSource
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class PushRepositoryImplTest {
    private lateinit var localDataSource: PushLocalDataSource
    private lateinit var remoteDataSource: PushRemoteDataSource
    private lateinit var tested: PushRepositoryImpl
    private lateinit var testDb: TestDatabase
    private lateinit var workManager: WorkManager

    @BeforeTest
    fun setUp() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        testDb = inMemoryDatabaseBuilder(appContext, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            .prepare()
        localDataSource = PushLocalDataSourceImpl(testDb)
        remoteDataSource = mockk()
        workManager = mockk(relaxed = true)
        tested = PushRepositoryImpl(remoteDataSource, localDataSource, workManager)
    }

    @AfterTest
    fun tearDown() {
        testDb.clearAllTables()
        testDb.close()
    }

    @Test
    fun `get Pushes by type, remotely`() = runBlocking {
        coEvery { remoteDataSource.getAllPushes(testUserId) } answers { allTestPushes }

        val results = tested.getAllPushes(testUserId, PushObjectType.Messages)
        val resultsFromDb = tested.getAllPushes(testUserId, PushObjectType.Messages)

        val messagePushes = listOf(testPush2)
        assertContentEquals(messagePushes, results)
        assertContentEquals(messagePushes, resultsFromDb)
        coVerify(exactly = 1) { remoteDataSource.getAllPushes(testUserId) }
    }

    @Test
    fun `delete a Push`() = runBlocking {
        coEvery { remoteDataSource.getAllPushes(testUserId) } answers { allTestPushes }
        every { workManager.enqueue(any<WorkRequest>()) } returns mockk()

        tested.getAllPushes(testUserId, PushObjectType.Messages)

        val deletedPush = testPush2
        tested.deletePush(testUserId, deletedPush.pushId)

        val allPushesAfterDeleting = tested.getAllPushes(testUserId, PushObjectType.Messages)
        assertContentEquals(testPushesMessages - deletedPush, allPushesAfterDeleting)

        val messagePushesAfterDeleting = tested.getAllPushes(testUserId, PushObjectType.Messages)
        assertTrue(messagePushesAfterDeleting.isEmpty())
    }

    @Test
    fun `delete a Push, then refresh remotely`() = runBlocking {
        coEvery { remoteDataSource.getAllPushes(testUserId) } answers { allTestPushes }
        every { workManager.enqueue(any<WorkRequest>()) } returns mockk()

        tested.getAllPushes(testUserId, PushObjectType.Messages)

        val deletedPush = testPush2
        tested.deletePush(testUserId, deletedPush.pushId)

        assertContentEquals(
            testPushesMessages - deletedPush,
            tested.getAllPushes(testUserId, PushObjectType.Messages)
        )

        assertContentEquals(
            testPushesMessages,
            tested.getAllPushes(testUserId, PushObjectType.Messages, refresh = true)
        )
    }

    @Test
    fun `delete users pushes`() = runBlocking {
        coEvery { remoteDataSource.getAllPushes(testUserId) } answers { allTestPushes }
        every { workManager.enqueue(any<WorkRequest>()) } returns mockk()

        val allPushes = tested.getAllPushes(testUserId, PushObjectType.Messages)
        assertContentEquals(testPushesMessages, allPushes)

        val messagePushes = tested.getAllPushes(testUserId, PushObjectType.Messages)
        assertContentEquals(listOf(testPush2), messagePushes)

        testPushesMessages.forEach { tested.deletePush(it.userId, it.pushId) }

        val allPushesAfterDeleting = tested.getAllPushes(testUserId, PushObjectType.Messages)
        assertTrue(allPushesAfterDeleting.isEmpty())

        val messagePushesAfterDeleting = tested.getAllPushes(testUserId, PushObjectType.Messages)
        assertTrue(messagePushesAfterDeleting.isEmpty())
    }

    @Test
    fun `get all remotely, then get all by type, locally`() = runBlocking {
        coEvery { remoteDataSource.getAllPushes(testUserId) } returns allTestPushes
        tested.getAllPushes(testUserId, PushObjectType.Messages)

        val messagePushes = tested.getAllPushes(testUserId, PushObjectType.Messages)
        assertContentEquals(listOf(testPush2), messagePushes)

        coVerify(exactly = 1) { remoteDataSource.getAllPushes(testUserId) }
    }

    @Test
    fun `delete a Push while observing pushes by type`() = runBlocking {
        val deletedItem = testPush2
        val job = launch {
            tested.observeAllPushes(testUserId, PushObjectType.Messages).test {
                assertContentEquals(testPushesMessages, awaitItem())
                assertContentEquals(testPushesMessages - deletedItem, awaitItem())
            }
        }

        tested.deletePush(testUserId, deletedItem.pushId)
        job.join()
    }
}
