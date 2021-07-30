/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.usersettings.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.usersettings.data.api.UserSettingsApi
import me.proton.core.usersettings.data.api.response.FlagsResponse
import me.proton.core.usersettings.data.api.response.PasswordResponse
import me.proton.core.usersettings.data.api.response.RecoverySettingResponse
import me.proton.core.usersettings.data.api.response.SingleUserSettingsResponse
import me.proton.core.usersettings.data.api.response.UserSettingsResponse
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import me.proton.core.usersettings.data.db.dao.UserSettingsDao
import me.proton.core.usersettings.data.extension.fromResponse
import me.proton.core.usersettings.data.extension.toEntity
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserSettingsRepositoryImplTest {

    // region mocks
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val userSettingsApi = mockk<UserSettingsApi>(relaxed = true)

    private val apiFactory = mockk<ApiManagerFactory>(relaxed = true)
    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: UserSettingsRepositoryImpl

    private val db = mockk<UserSettingsDatabase>(relaxed = true)
    private val userSettingsDao = mockk<UserSettingsDao>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"
    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        every { db.userSettingsDao() } returns userSettingsDao
        coEvery { sessionProvider.getSessionId(any()) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiFactory, sessionProvider)
        every { apiFactory.create(any(), interfaceClass = UserSettingsApi::class) } returns TestApiManager(
            userSettingsApi
        )

        repository = UserSettingsRepositoryImpl(db, apiProvider)
    }

    @Test
    fun `settings returns success`() = runBlockingTest {
        val settingsResponse = UserSettingsResponse(
            email = RecoverySettingResponse("test-email", 1, notify = 1, reset = 1),
            phone = null,
            twoFA = null,
            password = PasswordResponse(mode = 1, expirationTime = null),
            news = 0,
            locale = "en",
            logAuth = 1,
            density = 1,
            invoiceText = "",
            dateFormat = 1,
            timeFormat = 2,
            themeType = 1,
            weekStart = 7,
            welcome = 1,
            earlyAccess = 1,
            theme = "test-theme",
            flags = FlagsResponse(1)
        )
        // GIVEN
        coEvery { userSettingsApi.getUserSettings() } returns SingleUserSettingsResponse(settingsResponse)
        every { userSettingsDao.observeByUserId(any()) } returns flowOf(
            settingsResponse.fromResponse(UserId(testUserId)).toEntity()
        )
        // WHEN
        val response = repository.getUserSettings(sessionUserId = UserId(testUserId))
        // THEN
        assertNotNull(response)
        assertEquals("test-email", response.email!!.value)
        verify { userSettingsDao.observeByUserId(any()) }
    }


    @Test
    fun `update recovery email returns result success`() = runBlockingTest {
        // GIVEN
        val settingsResponse = UserSettingsResponse(
            email = RecoverySettingResponse("test-email2", 1, notify = 1, reset = 1),
            phone = null,
            twoFA = null,
            password = PasswordResponse(mode = 1, expirationTime = null),
            news = 0,
            locale = "en",
            logAuth = 1,
            density = 1,
            invoiceText = "",
            dateFormat = 1,
            timeFormat = 2,
            themeType = 1,
            weekStart = 7,
            welcome = 1,
            earlyAccess = 1,
            theme = "test-theme",
            flags = FlagsResponse(1)
        )
        coEvery { userSettingsApi.updateRecoveryEmail(any()) } returns SingleUserSettingsResponse(settingsResponse)
        every { userSettingsDao.observeByUserId(any()) } returns flowOf(
            settingsResponse.fromResponse(UserId(testUserId)).toEntity()
        )

        // WHEN
        val response = repository.updateRecoveryEmail(
            sessionUserId = UserId(testUserId),
            email = "test-email2",
            clientEphemeral = "test-client-empheral",
            clientProof = "test-client-proof",
            srpSession = "test-srp-session",
            secondFactorCode = ""
        )
        // THEN
        assertNotNull(response)
        assertEquals("test-email2", response.email!!.value)
        coVerify { userSettingsDao.insertOrUpdate(any()) }
        verify { userSettingsDao.observeByUserId(any()) }
    }
}