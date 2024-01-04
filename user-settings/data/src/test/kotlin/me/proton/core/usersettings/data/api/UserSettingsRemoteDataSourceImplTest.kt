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

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.TestDispatcherProvider
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


class UserSettingsRemoteDataSourceImplTest {

    private val dispatcherProvider = TestDispatcherProvider()

    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val userSettingsApi = mockk<UserSettingsApi>(relaxed = true)

    private val apiFactory = mockk<ApiManagerFactory>(relaxed = true)
    private val apiProvider = ApiProvider(apiFactory, sessionProvider, dispatcherProvider)

    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"

    private val remoteDataSource = UserSettingsRemoteDataSourceImpl(apiProvider)

    @Before
    fun beforeEveryTest() {
        // GIVEN
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

    private val settingsResponse = UserSettingsResponse(
        email = RecoverySettingResponse("test-email2", 1, notify = 1, reset = 1),
        phone = null,
        twoFA = null,
        password = PasswordResponse(mode = 1, expirationTime = null),
        news = 0,
        locale = "en",
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
