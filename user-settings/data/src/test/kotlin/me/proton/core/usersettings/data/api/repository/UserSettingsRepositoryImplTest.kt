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

package me.proton.core.usersettings.data.api.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.usersettings.data.api.UserSettingsApi
import me.proton.core.usersettings.domain.entity.Flags
import me.proton.core.usersettings.domain.entity.Password
import me.proton.core.usersettings.domain.entity.Setting
import me.proton.core.usersettings.domain.entity.UserSettings
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class UserSettingsRepositoryImplTest {

    // region mocks
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val apiFactory = mockk<ApiManagerFactory>(relaxed = true)
    private val apiManager = mockk<ApiManager<UserSettingsApi>>(relaxed = true)
    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: UserSettingsRepositoryImpl
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"
    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        coEvery { sessionProvider.getSessionId(UserId(testUserId)) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiFactory, sessionProvider)
        every {
            apiFactory.create(
                interfaceClass = UserSettingsApi::class
            )
        } returns apiManager
        every {
            apiFactory.create(
                SessionId(testSessionId),
                interfaceClass = UserSettingsApi::class
            )
        } returns apiManager
        repository = UserSettingsRepositoryImpl(apiProvider)
    }

    @Test
    fun `settings returns success`() = runBlockingTest {
        val settingsResponse = UserSettings(
            email = Setting("test-email", 1, 1, 1),
            phone = null,
            twoFA = null,
            password = Password(mode = 1, expirationTime = null),
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
            flags = Flags(1)
        )
        // GIVEN
        coEvery { apiManager.invoke<UserSettings>(any(), any()) } returns ApiResult.Success(settingsResponse)
        // WHEN
        val response = repository.getSettings(sessionUserId = UserId(testUserId))
        // THEN
        assertNotNull(response)
        assertEquals("test-email", response.email!!.value)
    }

    @Test
    fun `user settings returns error`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<UserSettings>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.getSettings(sessionUserId = UserId(testUserId))
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }

    @Test
    fun `update recovery email returns result success`() = runBlockingTest {
        val settingsResponse = UserSettings(
            email = Setting("test-email2", 1, 1, 1),
            phone = null,
            twoFA = null,
            password = Password(mode = 1, expirationTime = null),
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
            flags = Flags(1)
        )
        // GIVEN
        coEvery { apiManager.invoke<UserSettings>(any(), any()) } returns ApiResult.Success(settingsResponse)
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
    }

    @Test
    fun `update recovery email returns error`() = runBlockingTest {
        // GIVEN
        coEvery { apiManager.invoke<UserSettings>(any(), any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.updateRecoveryEmail(
                sessionUserId = UserId(testUserId),
                email = "test-email2",
                clientEphemeral = "test-client-empheral",
                clientProof = "test-client-proof",
                srpSession = "test-srp-session",
                secondFactorCode = ""
            )
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }
}