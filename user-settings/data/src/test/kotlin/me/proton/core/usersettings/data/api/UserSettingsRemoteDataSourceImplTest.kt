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

package me.proton.core.usersettings.data.api

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.data.api.request.SetRecoverySecretRequest
import me.proton.core.usersettings.data.api.request.UpdateCrashReportsRequest
import me.proton.core.usersettings.data.api.request.UpdateTelemetryRequest
import me.proton.core.usersettings.data.api.response.PasswordResponse
import me.proton.core.usersettings.data.api.response.RecoverySettingResponse
import me.proton.core.usersettings.data.api.response.SingleUserSettingsResponse
import me.proton.core.usersettings.data.api.response.UserSettingsResponse
import me.proton.core.usersettings.domain.entity.UserSettingsProperty
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class UserSettingsRemoteDataSourceImplTest {

    private val dispatcherProvider = TestDispatcherProvider()

    @MockK(relaxed = true)
    private lateinit var apiFactory: ApiManagerFactory

    @MockK(relaxed = true)
    private lateinit var sessionProvider: SessionProvider

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK(relaxed = true)
    private lateinit var userSettingsApi: UserSettingsApi

    private lateinit var apiProvider: ApiProvider

    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"

    private lateinit var remoteDataSource: UserSettingsRemoteDataSourceImpl

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)

        // GIVEN
        apiProvider = ApiProvider(apiFactory, sessionProvider, dispatcherProvider)
        remoteDataSource = UserSettingsRemoteDataSourceImpl(apiProvider, userRepository)

        coEvery { sessionProvider.getSessionId(any()) } returns SessionId(testSessionId)
        every { apiFactory.create(any(), interfaceClass = UserSettingsApi::class) } returns TestApiManager(
            userSettingsApi
        )
    }

    @Test
    fun `update crash reports returns success`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery {
            userSettingsApi.updateCrashReports(UpdateCrashReportsRequest(1))
        } returns SingleUserSettingsResponse(
            settings = settingsResponse,
        )

        // WHEN
        val response = remoteDataSource.updateUserSettings(
            userId = UserId(testUserId),
            property = UserSettingsProperty.CrashReports(true)
        )
        // THEN
        assertNotNull(response)
        assertEquals(true, response.crashReports)
    }

    @Test
    fun `update telemetry returns success`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery {
            userSettingsApi.updateTelemetry(UpdateTelemetryRequest(1))
        } returns SingleUserSettingsResponse(
            settings = settingsResponse,
        )

        // WHEN
        val response = remoteDataSource.updateUserSettings(
            userId = UserId(testUserId),
            property = UserSettingsProperty.Telemetry(true)
        )
        // THEN
        assertNotNull(response)
        assertEquals(true, response.telemetry)
    }

    @Test
    fun setRecoverySecret() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val secret = "secret"
        val signature = "signature"
        val request = SetRecoverySecretRequest(secret, signature)
        coEvery { userSettingsApi.setRecoverySecret(request) } returns GenericResponse(1000)

        // WHEN
        val response = remoteDataSource.setRecoverySecret(
            userId = UserId(testUserId),
            secret = secret,
            signature = signature
        )
        // THEN
        assertTrue(response)
    }

    private val settingsResponse = UserSettingsResponse.nil().copy(
        email = RecoverySettingResponse("test-email2", 1, notify = 1, reset = 1),
        password = PasswordResponse(mode = 1, expirationTime = null),
        logAuth = 1,
        density = 1,
        dateFormat = 1,
        timeFormat = 2,
        weekStart = 7,
        earlyAccess = 1,
        deviceRecovery = 1,
        telemetry = 1,
        crashReports = 1
    )
}
