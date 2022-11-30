/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonMail.
 *
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.mailsettings.data.worker

import java.io.IOException
import java.net.SocketTimeoutException
import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker.Result.Failure
import androidx.work.ListenableWorker.Result.Retry
import androidx.work.ListenableWorker.Result.Success
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.data.api.MailSettingsApi
import me.proton.core.mailsettings.data.api.request.UpdateAttachPublicKeyRequest
import me.proton.core.mailsettings.data.api.request.UpdateDisplayNameRequest
import me.proton.core.mailsettings.data.api.response.SingleMailSettingsResponse
import me.proton.core.mailsettings.data.testdata.MailSettingsTestData
import me.proton.core.mailsettings.data.worker.SettingsProperty.DisplayName
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.serialize
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class UpdateSettingsWorkerTest {

    private val sessionId = SessionId("sessionId")
    private val apiResponse = SingleMailSettingsResponse(MailSettingsTestData.apiResponse)

    private val context = mockk<Context>()
    private val parameters = mockk<WorkerParameters> {
        every { this@mockk.taskExecutor } returns mockk(relaxed = true)
    }
    private val workManager = mockk<WorkManager> {
        coEvery {
            this@mockk.enqueueUniqueWork(
                any<String>(),
                any<ExistingWorkPolicy>(),
                any<OneTimeWorkRequest>()
            )
        } returns mockk()
        every { this@mockk.getWorkInfoById(any()) } returns mockk()
    }

    private val mailSettingsApi = mockk<MailSettingsApi>()
    private val sessionProvider = mockk<SessionProvider> {
        coEvery { this@mockk.getSessionId(any()) } returns sessionId
    }
    private val apiManagerFactory = mockk<ApiManagerFactory> {
        every { this@mockk.create(any(), MailSettingsApi::class) } returns TestApiManager(mailSettingsApi)
    }

    private val dispatcherProvider = TestDispatcherProvider()

    private val worker = UpdateSettingsWorker(
        context,
        parameters,
        ApiProvider(apiManagerFactory, sessionProvider, dispatcherProvider)
    )

    @Test
    fun `worker enqueuer creates one time request worker which is unique by user and setting property`() {
        // WHEN
        val userId = UserId("userId")
        UpdateSettingsWorker.Enqueuer(workManager).enqueue(userId, DisplayName("Updated name"))

        // THEN
        val requestSlot = slot<OneTimeWorkRequest>()
        verify {
            workManager.enqueueUniqueWork(
                "updateSettingsWork-userId-DisplayName",
                ExistingWorkPolicy.REPLACE,
                capture(requestSlot)
            )
        }
        val workSpec = requestSlot.captured.workSpec
        val inputData = workSpec.input
        val actualUserId = inputData.getString("keyUserId")
        val settingsPropertySerialized = inputData.getString("keySettingsPropertySerialized")
        val actualSettingsProperty = settingsPropertySerialized?.deserialize<SettingsProperty>()
        assertEquals(NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
        assertEquals("userId", actualUserId)
        assertEquals(DisplayName("Updated name"), actualSettingsProperty)
    }

    @Test
    fun `worker executes updateDisplayName API call when given settingsProperty is DisplayName`() =
        runTest(dispatcherProvider.Main) {
            // GIVEN
            userIdInputIs("userId")
            settingPropertyInputIs(DisplayName("updated name"))
            coEvery { mailSettingsApi.updateDisplayName(any()) } returns apiResponse

            // WHEN
            worker.doWork()

            // THEN
            val expectedRequest = UpdateDisplayNameRequest("updated name")
            coVerify { mailSettingsApi.updateDisplayName(expectedRequest) }
        }

    @Test
    fun `worker executes updateAttachPublicKey API call when given settingsProperty is AttachPublicKey`() =
        runTest(dispatcherProvider.Main) {
            // GIVEN
            userIdInputIs("userId")
            settingPropertyInputIs(SettingsProperty.AttachPublicKey(0))
            coEvery { mailSettingsApi.updateAttachPublicKey(any()) } returns apiResponse

            // WHEN
            worker.doWork()

            // THEN
            val expectedRequest = UpdateAttachPublicKeyRequest(0)
            coVerify { mailSettingsApi.updateAttachPublicKey(expectedRequest) }
        }

    @Test
    fun `worker returns Success when API call succeeds`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        userIdInputIs("userId")
        settingPropertyInputIs(SettingsProperty.PromptPin(0))
        coEvery { mailSettingsApi.updatePromptPin(any()) } returns apiResponse

        // WHEN
        val result = worker.doWork()

        // THEN
        assertEquals(Success.success(), result)
    }

    @Test
    fun `worker returns Failure when API call fails and maxRetries were reached`() =
        runTest(dispatcherProvider.Main) {
            // GIVEN
            userIdInputIs("userId")
            settingPropertyInputIs(SettingsProperty.ShowImages(3))
            coEvery { mailSettingsApi.updateShowImages(any()) } throws IOException("Retryable error")
            every { parameters.runAttemptCount } returns 5

            // WHEN
            val result = worker.doWork()

            // THEN
            assertEquals(Failure.failure(), result)
        }

    @Test
    fun `worker returns Failure when API call fails and result is not retryable`() =
        runTest(dispatcherProvider.Main) {
            // GIVEN
            userIdInputIs("userId")
            settingPropertyInputIs(SettingsProperty.ShowImages(3))
            coEvery { mailSettingsApi.updateShowImages(any()) } throws SerializationException("Non retryable error")
            every { parameters.runAttemptCount } returns 0

            // WHEN
            val result = worker.doWork()

            // THEN
            assertEquals(Failure.failure(), result)
        }

    @Test
    fun `worker returns Retry when API call failed and maxRetries were not reached and result is retryable`() =
        runTest(dispatcherProvider.Main) {
            // GIVEN
            userIdInputIs("userId")
            settingPropertyInputIs(SettingsProperty.DraftMimeType("text/plain"))
            coEvery { mailSettingsApi.updateDraftMimeType(any()) } throws SocketTimeoutException("Retryable error")
            every { parameters.runAttemptCount } returns 1

            // WHEN
            val result = worker.doWork()

            // THEN
            assertEquals(Retry.retry(), result)
        }

    @Test
    fun `worker returns Failure when API call throw unexpected exception`() =
        runTest(dispatcherProvider.Main) {
            // GIVEN
            userIdInputIs("userId")
            settingPropertyInputIs(SettingsProperty.ShowImages(3))
            coEvery { mailSettingsApi.updateShowImages(any()) } throws IllegalStateException("error")
            every { parameters.runAttemptCount } returns 5

            // WHEN
            assertFailsWith(IllegalStateException::class) {
                worker.doWork()
            }
        }

    private fun settingPropertyInputIs(displayNameProperty: SettingsProperty) {
        // When serializing polymorphic class hierarchies you must ensure that the compile-time type
        // of the serialized object is a polymorphic one, not a concrete one.
        // (https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#sealed-classes)
        every {
            parameters.inputData.getString("keySettingsPropertySerialized")
        } returns displayNameProperty.serialize()
    }

    private fun userIdInputIs(rawUserId: String) {
        every { parameters.inputData.getString("keyUserId") } returns rawUserId
    }
}
