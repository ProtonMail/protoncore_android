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

package me.proton.core.keytransparency.domain

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.keytransparency.domain.entity.SelfAuditResult
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.keytransparency.domain.usecase.GetCurrentTime
import me.proton.core.keytransparency.domain.usecase.IsKeyTransparencyEnabled
import me.proton.core.keytransparency.domain.usecase.LogKeyTransparency
import me.proton.core.keytransparency.domain.usecase.SelfAudit
import me.proton.core.user.domain.UserManager
import org.junit.Test
import kotlin.test.BeforeTest

class RunSelfAuditTest {
    private lateinit var runSelfAudit: RunSelfAudit
    private val userManager: UserManager = mockk()
    private val isKeyTransparencyEnabled = mockk<IsKeyTransparencyEnabled>()
    private val selfAudit = mockk<SelfAudit>()
    private val logKeyTransparency = mockk<LogKeyTransparency>()
    private val keyTransparencyRepository = mockk<KeyTransparencyRepository>()
    private val getCurrentTime = mockk<GetCurrentTime>()

    @BeforeTest
    fun setUp() {
        runSelfAudit = RunSelfAudit(
            userManager,
            isKeyTransparencyEnabled,
            selfAudit,
            logKeyTransparency,
            keyTransparencyRepository,
            getCurrentTime
        )
    }

    @Test
    fun `when KT is deactivated, do nothing`() = runTest {
        // given
        val userId = UserId("test-user-id")
        coEvery { isKeyTransparencyEnabled() } returns false
        // when
        runSelfAudit(userId)
        // then
        coVerify(exactly = 0) {
            selfAudit(userId, any())
        }
    }

    @Test
    fun `when self audit ran recently, do nothing`() = runTest {
        // given
        val userId = UserId("test-user-id")
        coEvery { isKeyTransparencyEnabled() } returns true
        val selfAuditTimestamp = 1000L
        coEvery { keyTransparencyRepository.getTimestampOfSelfAudit(userId) } returns selfAuditTimestamp
        val currentTime = selfAuditTimestamp + 10L
        coEvery { getCurrentTime() } returns currentTime
        // when
        runSelfAudit(userId)
        // then
        coVerify(exactly = 0) {
            selfAudit(userId, any())
        }
    }

    @Test
    fun `when self audit ran too long ago, run self audit`() = runTest {
        // given
        val userId = UserId("test-user-id")
        coEvery { isKeyTransparencyEnabled() } returns true
        val selfAuditTimestamp = 1000L
        coEvery { keyTransparencyRepository.getTimestampOfSelfAudit(userId) } returns selfAuditTimestamp
        val currentTime = selfAuditTimestamp + Constants.KT_SELF_AUDIT_INTERVAL_SECONDS + 10
        coEvery { getCurrentTime() } returns currentTime
        coEvery { userManager.getAddresses(userId, any()) } returns listOf(
            mockk {
                every { email } returns "email"
            }
        )
        val result = mockk<SelfAuditResult.Success>()
        coEvery { selfAudit(userId, any()) } returns result
        coJustRun { logKeyTransparency.logSelfAuditResult(result) }
        coJustRun { keyTransparencyRepository.storeSelfAuditResult(userId, result) }
        // when
        runSelfAudit(userId)
        // then
        coVerify(exactly = 1) {
            userManager.getAddresses(userId, any())
            selfAudit(userId, any())
            logKeyTransparency.logSelfAuditResult(result)
            keyTransparencyRepository.storeSelfAuditResult(userId, result)
        }
    }

    @Test
    fun `when self audit is forced refresh, run self audit`() = runTest {
        // given
        val userId = UserId("test-user-id")
        coEvery { isKeyTransparencyEnabled() } returns true
        val selfAuditTimestamp = 1000L
        coEvery { keyTransparencyRepository.getTimestampOfSelfAudit(userId) } returns selfAuditTimestamp
        val currentTime = selfAuditTimestamp + 10
        coEvery { getCurrentTime() } returns currentTime
        coEvery { userManager.getAddresses(userId, any()) } returns listOf(
            mockk {
                every { email } returns "email"
            }
        )
        val result = mockk<SelfAuditResult.Success>()
        coEvery { selfAudit(userId, any()) } returns result
        coJustRun { logKeyTransparency.logSelfAuditResult(result) }
        coJustRun { keyTransparencyRepository.storeSelfAuditResult(userId, result) }
        // when
        runSelfAudit(userId, forceRefresh = true)
        // then
        coVerify(exactly = 1) {
            userManager.getAddresses(userId, any())
            selfAudit(userId, any())
            logKeyTransparency.logSelfAuditResult(result)
            keyTransparencyRepository.storeSelfAuditResult(userId, result)
        }
    }

}
