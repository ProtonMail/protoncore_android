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

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.exception.InvalidServerAuthenticationException
import me.proton.core.auth.domain.usecase.ValidateServerProof
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.android.api.TestApiManager
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.usersettings.data.api.UserSettingsApi
import me.proton.core.usersettings.data.api.UserSettingsRemoteDataSourceImpl
import me.proton.core.usersettings.data.api.request.UpdateTelemetryRequest
import me.proton.core.usersettings.data.api.response.PasswordResponse
import me.proton.core.usersettings.data.api.response.RecoverySettingResponse
import me.proton.core.usersettings.data.api.response.SingleUserSettingsResponse
import me.proton.core.usersettings.data.api.response.UpdateUserSettingsResponse
import me.proton.core.usersettings.data.api.response.UserSettingsResponse
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import me.proton.core.usersettings.data.db.UserSettingsLocalDataSourceImpl
import me.proton.core.usersettings.data.db.dao.UserSettingsDao
import me.proton.core.usersettings.data.extension.fromResponse
import me.proton.core.usersettings.data.extension.toEntity
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    private val workManager = mockk<WorkManager>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"
    private val testSrpProofs = SrpProofs(
        clientEphemeral = "test-client-ephemeral",
        clientProof = "test-client-proof",
        expectedServerProof = "test-server-proof"
    )

    // endregion

    private val validateServerProof = ValidateServerProof()

    private val dispatcherProvider = TestDispatcherProvider()

    @Before
    fun beforeEveryTest() {
        // GIVEN
        every { db.userSettingsDao() } returns userSettingsDao
        coEvery { sessionProvider.getSessionId(any()) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiFactory, sessionProvider, dispatcherProvider)
        every { apiFactory.create(any(), interfaceClass = UserSettingsApi::class) } returns TestApiManager(
            userSettingsApi
        )

        repository = UserSettingsRepositoryImpl(
            localDataSource = UserSettingsLocalDataSourceImpl(db),
            remoteDataSource = UserSettingsRemoteDataSourceImpl(apiProvider),
            validateServerProof = validateServerProof,
            workManager = workManager,
            scopeProvider = TestCoroutineScopeProvider(dispatcherProvider)
        )
    }

    @Test
    fun `user settings returns success`() = runTest(dispatcherProvider.Main) {
        val settingsResponse = UserSettingsResponse(
            email = RecoverySettingResponse("test-email", 1, notify = 1, reset = 1),
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
    fun `update recovery email returns result success`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        setUpRecoveryEmailUpdateTest(testSrpProofs.expectedServerProof)

        // WHEN
        val response = repository.updateRecoveryEmail(
            sessionUserId = UserId(testUserId),
            email = "test-email2",
            srpProofs = testSrpProofs,
            srpSession = "test-srp-session",
            secondFactorCode = ""
        )
        // THEN
        assertNotNull(response)
        assertEquals("test-email2", response.email!!.value)
        coVerify { userSettingsDao.insertOrUpdate(any()) }
        verify { userSettingsDao.observeByUserId(any()) }
    }

    @Test
    fun `update recovery email fails with wrong server proof`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        setUpRecoveryEmailUpdateTest(testSrpProofs.expectedServerProof + "corrupted")

        // WHEN & THEN
        val exception = assertFailsWith<InvalidServerAuthenticationException> {
            repository.updateRecoveryEmail(
                sessionUserId = UserId(testUserId),
                email = "test-email2",
                srpProofs = testSrpProofs,
                srpSession = "test-srp-session",
                secondFactorCode = ""
            )
        }
        assertEquals(
            "Server returned invalid srp proof, recovery email update failed",
            exception.message
        )
    }

    private fun setUpRecoveryEmailUpdateTest(srpServerProof: String) {
        val settingsResponse = UserSettingsResponse(
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
        coEvery { userSettingsApi.updateRecoveryEmail(any()) } returns UpdateUserSettingsResponse(
            settings = settingsResponse,
            serverProof = srpServerProof
        )
        every { userSettingsDao.observeByUserId(any()) } returns flowOf(
            settingsResponse.fromResponse(UserId(testUserId)).toEntity()
        )
    }

    @Test
    fun `update login password returns success`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val testAuth = setUpUpdatePasswordTest(testSrpProofs.expectedServerProof)

        // WHEN
        val response = repository.updateLoginPassword(
            sessionUserId = UserId(testUserId),
            srpProofs = testSrpProofs,
            srpSession = "test-srp-session",
            secondFactorCode = "",
            auth = testAuth
        )
        // THEN
        assertNotNull(response)
        assertEquals("test-email2", response.email!!.value)
        coVerify { userSettingsDao.insertOrUpdate(any()) }
        verify { userSettingsDao.observeByUserId(any()) }
    }

    @Test
    fun `update login password fails with wrong server proof`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val testAuth = setUpUpdatePasswordTest(testSrpProofs.expectedServerProof + "corrupted")

        // WHEN & THEN
        val exception = assertFailsWith<InvalidServerAuthenticationException> {
            repository.updateLoginPassword(
                sessionUserId = UserId(testUserId),
                srpProofs = testSrpProofs,
                srpSession = "test-srp-session",
                secondFactorCode = "",
                auth = testAuth
            )
        }
        assertEquals(
            "Server returned invalid srp proof, password change failed",
            exception.message
        )
    }

    private fun setUpUpdatePasswordTest(srpServerProof: String): Auth {
        val settingsResponse = UserSettingsResponse(
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
        val testSalt = "test-salt"
        val testModulusId = "test-modulus-id"
        val testAuth = Auth(
            version = 1,
            modulusId = testModulusId,
            salt = testSalt,
            verifier = "test-verifier"
        )
        coEvery { userSettingsApi.updateLoginPassword(any()) } returns UpdateUserSettingsResponse(
            settings = settingsResponse,
            serverProof = srpServerProof
        )
        every { userSettingsDao.observeByUserId(any()) } returns flowOf(
            settingsResponse.fromResponse(UserId(testUserId)).toEntity()
        )
        return testAuth
    }

    @Test
    fun `update crash reports returns success`() = runTest(dispatcherProvider.Main) {
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
            dateFormat = 1,
            timeFormat = 2,
            weekStart = 7,
            earlyAccess = 1,
            deviceRecovery = 1,
            telemetry = 1,
            crashReports = 1
        )
        every { userSettingsDao.observeByUserId(any()) } returns flowOf(
            settingsResponse.fromResponse(UserId(testUserId)).toEntity()
        )

        // WHEN
        val response = repository.updateCrashReports(
            userId = UserId(testUserId),
            isEnabled = true
        )
        // THEN
        assertNotNull(response)
        assertEquals(true, response.crashReports)
        coVerify { userSettingsDao.insertOrUpdate(response.toEntity()) }
        verify {
            workManager.enqueueUniqueWork(
                "updateUserSettingsWork-test-user-id-CrashReports",
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `update telemetry returns success`() = runTest(dispatcherProvider.Main) {
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
            dateFormat = 1,
            timeFormat = 2,
            weekStart = 7,
            earlyAccess = 1,
            deviceRecovery = 1,
            telemetry = 1,
            crashReports = 1
        )
        coEvery {
            userSettingsApi.updateTelemetry(UpdateTelemetryRequest(1))
        } returns SingleUserSettingsResponse(
            settings = settingsResponse,
        )
        every { userSettingsDao.observeByUserId(any()) } returns flowOf(
            settingsResponse.fromResponse(UserId(testUserId)).toEntity()
        )

        // WHEN
        val response = repository.updateTelemetry(
            userId = UserId(testUserId),
            isEnabled = true
        )
        // THEN
        assertNotNull(response)
        assertEquals(true, response.telemetry)
        coVerify { userSettingsDao.insertOrUpdate(response.toEntity()) }
        verify {
            workManager.enqueueUniqueWork(
                "updateUserSettingsWork-test-user-id-Telemetry",
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun markAsStale() = runTest(dispatcherProvider.Main) {
        // WHEN
        repository.markAsStale(UserId(testUserId))
        // THEN
        verify {
            workManager.enqueueUniqueWork(
                "freshUserSettingsWork-test-user-id",
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun setRecoverySecret() = runTest(dispatcherProvider.Main) {
        // WHEN
        repository.setRecoverySecret(UserId(testUserId))
        // THEN
        verify {
            workManager.enqueueUniqueWork(
                "setRecoverySecretWork-test-user-id",
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
    }
}
