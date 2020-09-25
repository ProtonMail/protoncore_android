/*
 * Copyright (c) 2020 Proton Technologies AG
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
package me.proton.core.humanverification.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.humanverification.data.api.HumanVerificationApi
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.exception.EmptyDestinationException
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.di.ApiFactory
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
@ExperimentalCoroutinesApi
class HumanVerificationRemoteRepositoryImplTest {

    private val sessionId: SessionId = SessionId("id")
    private val testPhoneNumber = "+123456789"
    private val testEmailAddress = "test@email.com"

    private val successResponse = GenericResponse(1000)
    private val errorResponse = "test error response"
    private val errorResponseCode = 422

    @RelaxedMockK
    private lateinit var apiFactory: ApiFactory
    private lateinit var apiProvider: ApiProvider

    @RelaxedMockK
    private lateinit var apiManager: ApiManager<HumanVerificationApi>

    @Before
    fun before() {
        MockKAnnotations.init(this)
        apiProvider = ApiProvider(apiFactory)
        every { apiFactory.create(sessionId, HumanVerificationApi::class) } returns apiManager
    }

    @Test(expected = EmptyDestinationException::class)
    fun `send code pass empty email`() = runBlockingTest {
        val remoteRepository =
            HumanVerificationRemoteRepositoryImpl(apiProvider)
        remoteRepository.sendVerificationCodeEmailAddress(sessionId, "")
    }

    @Test(expected = EmptyDestinationException::class)
    fun `send code pass empty sms`() = runBlockingTest {
        val remoteRepository =
            HumanVerificationRemoteRepositoryImpl(apiProvider)
        remoteRepository.sendVerificationCodePhoneNumber(sessionId, "")
    }

    @Test
    fun `send code pass email`() = runBlockingTest {
        val remoteRepository =
            HumanVerificationRemoteRepositoryImpl(apiProvider)
        val mockedResult = ApiResult.Success(successResponse)
        coEvery { apiManager.invoke<GenericResponse>(any(), any()) } returns mockedResult

        val result = remoteRepository.sendVerificationCodeEmailAddress(sessionId, testEmailAddress)
        assertEquals(VerificationResult.Success, result)
    }

    @Test
    fun `send code pass sms`() = runBlockingTest {
        val remoteRepository =
            HumanVerificationRemoteRepositoryImpl(apiProvider)
        val mockedResult = ApiResult.Success(successResponse)
        coEvery { apiManager.invoke<GenericResponse>(any(), any()) } returns mockedResult

        val result = remoteRepository.sendVerificationCodePhoneNumber(sessionId, testPhoneNumber)
        assertEquals(VerificationResult.Success, result)
    }

    @Test
    fun `send code pass email error`() = runBlockingTest {
        val remoteRepository =
            HumanVerificationRemoteRepositoryImpl(apiProvider)
        val mockedResult = ApiResult.Error.Http(errorResponseCode, errorResponse)
        coEvery { apiManager.invoke<Any>(any(), any()) } returns mockedResult

        val result = remoteRepository.sendVerificationCodeEmailAddress(sessionId, testEmailAddress)
        assertTrue(result is VerificationResult.Error)
    }

    @Test
    fun `send code pass sms error`() = runBlockingTest {
        val remoteRepository =
            HumanVerificationRemoteRepositoryImpl(apiProvider)
        val mockedResult = ApiResult.Error.Http(errorResponseCode, errorResponse)
        coEvery { apiManager.invoke<Any>(any(), any()) } returns mockedResult

        val result = remoteRepository.sendVerificationCodePhoneNumber(sessionId, testPhoneNumber)
        assertTrue(result is VerificationResult.Error)
    }

    @Test
    fun `send verification token sms`() = runBlockingTest {
        val remoteRepository =
            HumanVerificationRemoteRepositoryImpl(apiProvider)
        val mockedResult = ApiResult.Success(successResponse)
        coEvery { apiManager.invoke<GenericResponse>(any(), any()) } returns mockedResult
        val result = remoteRepository.sendVerificationCodePhoneNumber(sessionId, testPhoneNumber)
        assertEquals(VerificationResult.Success, result)
    }

    @Test
    fun `send verification token email`() = runBlockingTest {
        val remoteRepository =
            HumanVerificationRemoteRepositoryImpl(apiProvider)
        val mockedResult = ApiResult.Success(successResponse)
        coEvery { apiManager.invoke<GenericResponse>(any(), any()) } returns mockedResult
        val result = remoteRepository.sendVerificationCodeEmailAddress(sessionId, testEmailAddress)
        assertEquals(VerificationResult.Success, result)
    }

    @Test
    fun `send verification token error sms`() = runBlockingTest {
        val remoteRepository =
            HumanVerificationRemoteRepositoryImpl(apiProvider)
        val mockedResult = ApiResult.Error.Http(errorResponseCode, errorResponse)
        coEvery { apiManager.invoke<Any>(any(), any()) } returns mockedResult
        val result = remoteRepository.sendVerificationCodePhoneNumber(sessionId, testPhoneNumber)
        assertTrue(result is VerificationResult.Error)
    }

    @Test
    fun `send verification token error email`() = runBlockingTest {
        val remoteRepository =
            HumanVerificationRemoteRepositoryImpl(apiProvider)
        val mockedResult = ApiResult.Error.Http(errorResponseCode, errorResponse)
        coEvery { apiManager.invoke<Any>(any(), any()) } returns mockedResult
        val result = remoteRepository.sendVerificationCodeEmailAddress(sessionId, testEmailAddress)
        assertTrue(result is VerificationResult.Error)
    }
}
