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

package me.proton.core.humanverification.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import me.proton.core.humanverification.data.api.UserVerificationApi
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.repository.UserVerificationRepository
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Before
import org.junit.Test

class UserVerificationRepositoryImplTest {

    private val sessionId: SessionId = SessionId("id")
    private val clientId = ClientId.AccountSession(sessionId)

    private val testPhoneNumber = "+123456789"
    private val testEmailAddress = "test@email.com"

    private val successResponse = GenericResponse(1000)
    private val errorResponse = "test error response"
    private val errorResponseCode = 422

    @RelaxedMockK
    private lateinit var clientIdProvider: ClientIdProvider

    @RelaxedMockK
    private lateinit var sessionProvider: SessionProvider

    @RelaxedMockK
    private lateinit var apiManagerFactory: ApiManagerFactory
    private lateinit var apiProvider: ApiProvider

    @RelaxedMockK
    private lateinit var apiManager: ApiManager<UserVerificationApi>

    private val verificationTypeEmail = TokenType.EMAIL
    private val verificationTypeSms = TokenType.SMS

    private lateinit var remoteRepository: UserVerificationRepository

    private val dispatcherProvider = TestDispatcherProvider()

    @Before
    fun before() {
        MockKAnnotations.init(this)
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, dispatcherProvider)

        coEvery { clientIdProvider.getClientId(any()) } returns clientId
        every { apiManagerFactory.create(sessionId, UserVerificationApi::class) } returns apiManager

        remoteRepository = UserVerificationRepositoryImpl(apiProvider)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `send code pass empty email`() = runTest(dispatcherProvider.Main) {
        remoteRepository.sendVerificationCodeEmailAddress(sessionId, "", verificationTypeEmail)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `send code pass empty sms`() = runTest(dispatcherProvider.Main) {
        remoteRepository.sendVerificationCodePhoneNumber(sessionId, "", verificationTypeSms)
    }

    @Test
    fun `send code pass email`() = runTest(dispatcherProvider.Main) {
        val mockedResult = ApiResult.Success(successResponse)
        coEvery { apiManager.invoke<GenericResponse>(any(), any()) } returns mockedResult

        remoteRepository.sendVerificationCodeEmailAddress(sessionId, testEmailAddress, verificationTypeEmail)
    }

    @Test
    fun `send code pass sms`() = runTest(dispatcherProvider.Main) {
        val mockedResult = ApiResult.Success(successResponse)
        coEvery { apiManager.invoke<GenericResponse>(any(), any()) } returns mockedResult

        remoteRepository.sendVerificationCodePhoneNumber(sessionId, testPhoneNumber, verificationTypeSms)
    }

    @Test(expected = ApiException::class)
    fun `send code pass email error`() = runTest(dispatcherProvider.Main) {
        val mockedResult = ApiResult.Error.Http(errorResponseCode, errorResponse)
        coEvery { apiManager.invoke<Any>(any(), any()) } returns mockedResult

        remoteRepository.sendVerificationCodeEmailAddress(sessionId, testEmailAddress, verificationTypeEmail)
    }

    @Test(expected = ApiException::class)
    fun `send code pass sms error`() = runTest(dispatcherProvider.Main) {
        val mockedResult = ApiResult.Error.Http(errorResponseCode, errorResponse)
        coEvery { apiManager.invoke<Any>(any(), any()) } returns mockedResult

        remoteRepository.sendVerificationCodePhoneNumber(sessionId, testPhoneNumber, verificationTypeSms)
    }

    @Test
    fun `send verification token sms`() = runTest(dispatcherProvider.Main) {
        val mockedResult = ApiResult.Success(successResponse)
        coEvery { apiManager.invoke<GenericResponse>(any(), any()) } returns mockedResult

        remoteRepository.sendVerificationCodePhoneNumber(sessionId, testPhoneNumber, verificationTypeSms)
    }

    @Test
    fun `send verification token email`() = runTest(dispatcherProvider.Main) {
        val mockedResult = ApiResult.Success(successResponse)
        coEvery { apiManager.invoke<GenericResponse>(any(), any()) } returns mockedResult

        remoteRepository.sendVerificationCodeEmailAddress(sessionId, testEmailAddress, verificationTypeEmail)
    }

    @Test(expected = ApiException::class)
    fun `send verification token error sms`() = runTest(dispatcherProvider.Main) {
        val mockedResult = ApiResult.Error.Http(errorResponseCode, errorResponse)
        coEvery { apiManager.invoke<Any>(any(), any()) } returns mockedResult

        remoteRepository.sendVerificationCodePhoneNumber(sessionId, testPhoneNumber, verificationTypeSms)
    }

    @Test(expected = ApiException::class)
    fun `send verification token error email`() = runTest(dispatcherProvider.Main) {
        val mockedResult = ApiResult.Error.Http(errorResponseCode, errorResponse)
        coEvery { apiManager.invoke<Any>(any(), any()) } returns mockedResult

        remoteRepository.sendVerificationCodeEmailAddress(sessionId, testEmailAddress, verificationTypeEmail)
    }
}
