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

package me.proton.core.report.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.report.data.api.ReportApi
import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.domain.entity.BugReportMeta
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

internal class ReportRepositoryImplTest {
    private lateinit var mockApiManagerFactory: ApiManagerFactory
    private lateinit var mockApiManager: ApiManager<ReportApi>
    private lateinit var mockApiProvider: ApiProvider
    private lateinit var mockSessionProvider: SessionProvider
    private lateinit var tested: ReportRepositoryImpl

    private val testBugReport = BugReport(
        title = "title",
        description = "description",
        username = "username",
        email = "email@test"
    )
    private val testBugReportMeta = BugReportMeta(
        appVersionName = "android-mail@1.2.3",
        clientName = "TestApp",
        osName = "Android",
        osVersion = "12",
        Product.Mail
    )

    private val dispatcherProvider = TestDispatcherProvider

    @Before
    fun setUp() {
        mockApiManager = mockk()
        mockSessionProvider = mockk()
        mockApiManagerFactory = mockk {
            every { create(any(), ReportApi::class) } returns mockApiManager
        }
        mockApiProvider = ApiProvider(mockApiManagerFactory, mockSessionProvider, dispatcherProvider)
        tested = ReportRepositoryImpl(mockApiProvider)
    }

    @Test
    fun `successful report`() = runTest(dispatcherProvider.Main) {
        coEvery {
            mockApiManager.invoke<GenericResponse>(
                any(),
                any()
            )
        } returns ApiResult.Success(GenericResponse(ResponseCodes.OK))
        tested.sendReport(testBugReport, testBugReportMeta)
    }

    @Test
    fun `failed report http error`() = runTest(dispatcherProvider.Main) {
        coEvery {
            mockApiManager.invoke<GenericResponse>(
                any(),
                any()
            )
        } returns ApiResult.Error.Http(400, "Bad request", ApiResult.Error.ProtonData(123, "Invalid data"))

        val throwable = assertFailsWith<ApiException> {
            tested.sendReport(testBugReport, testBugReportMeta)
        }
        assertEquals("Invalid data", throwable.message)
    }

    @Test
    fun `failed report bad result`() = runTest(dispatcherProvider.Main) {
        coEvery {
            mockApiManager.invoke<GenericResponse>(
                any(),
                any()
            )
        } returns ApiResult.Success(GenericResponse(ResponseCodes.NOT_ALLOWED))

        assertFailsWith<IllegalStateException> {
            tested.sendReport(testBugReport, testBugReportMeta)
        }
    }
}
