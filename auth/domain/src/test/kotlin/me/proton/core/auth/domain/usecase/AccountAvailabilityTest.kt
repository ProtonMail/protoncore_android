package me.proton.core.auth.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.SignupEmailAvailabilityTotal
import me.proton.core.observability.domain.metrics.SignupFetchDomainsTotal
import me.proton.core.observability.domain.metrics.SignupUsernameAvailabilityTotal
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class AccountAvailabilityTest {
    private lateinit var tested: AccountAvailability

    @MockK
    private lateinit var domainRepository: DomainRepository

    @MockK(relaxUnitFun = true)
    private lateinit var observabilityManager: ObservabilityManager

    @MockK
    private lateinit var userRepository: UserRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = AccountAvailability(userRepository, domainRepository, observabilityManager)
    }

    @Test
    fun `getDomains observability success`() = runTest {
        // GIVEN
        coEvery { domainRepository.getAvailableDomains() } returns listOf("a", "b")

        // WHEN
        tested.getDomains(metricData = { SignupFetchDomainsTotal(it.toHttpApiStatus()) })

        // THEN
        verify {
            observabilityManager.enqueue(SignupFetchDomainsTotal(HttpApiStatus.http2xx), any())
        }
    }

    @Test
    fun `getDomains observability 4xx failure`() = runTest {
        // GIVEN
        coEvery { domainRepository.getAvailableDomains() } throws ApiException(
            ApiResult.Error.Http(400, "Bad request")
        )

        // WHEN
        assertFailsWith<ApiException> {
            tested.getDomains(metricData = { SignupFetchDomainsTotal(it.toHttpApiStatus()) })
        }

        // THEN
        verify {
            observabilityManager.enqueue(SignupFetchDomainsTotal(HttpApiStatus.http4xx), any())
        }
    }

    @Test
    fun `checkUsername observability success`() = runTest {
        // GIVEN
        coEvery { userRepository.getUser(any()) } returns mockk {
            every { name } returns null
        }
        coEvery { userRepository.checkUsernameAvailable(any()) } just runs

        // WHEN
        tested.checkUsername(
            userId = UserId("123"),
            username = "test-user",
            metricData = { SignupUsernameAvailabilityTotal(it.toHttpApiStatus()) }
        )

        // THEN
        verify {
            observabilityManager.enqueue(
                SignupUsernameAvailabilityTotal(HttpApiStatus.http2xx),
                any()
            )
        }
    }

    @Test
    fun `checkUsername observability failure`() = runTest {
        // GIVEN
        coEvery { userRepository.getUser(any()) } returns mockk {
            every { name } returns null
        }
        coEvery { userRepository.checkUsernameAvailable(any()) } throws ApiException(
            ApiResult.Error.Connection()
        )

        // WHEN
        assertFailsWith<ApiException> {
            tested.checkUsername(
                userId = UserId("123"),
                username = "test-user",
                metricData = { SignupUsernameAvailabilityTotal(it.toHttpApiStatus()) }
            )
        }

        // THEN
        verify {
            observabilityManager.enqueue(
                SignupUsernameAvailabilityTotal(HttpApiStatus.notConnected),
                any()
            )
        }
    }

    @Test
    fun `checkExternalEmail observability success`() = runTest {
        // GIVEN
        coEvery { userRepository.checkExternalEmailAvailable(any()) } just runs

        // WHEN
        tested.checkExternalEmail(
            email = "test@email.test",
            metricData = { SignupEmailAvailabilityTotal(it.toHttpApiStatus()) }
        )

        // THEN
        verify {
            observabilityManager.enqueue(
                SignupEmailAvailabilityTotal(HttpApiStatus.http2xx),
                any()
            )
        }
    }

    @Test
    fun `checkExternalEmail observability failure`() = runTest {
        // GIVEN
        coEvery { userRepository.checkExternalEmailAvailable(any()) } throws ApiException(
            ApiResult.Error.Http(500, "Server error")
        )

        // WHEN
        assertFailsWith<ApiException> {
            tested.checkExternalEmail(
                email = "test@email.test",
                metricData = { SignupEmailAvailabilityTotal(it.toHttpApiStatus()) }
            )
        }

        // THEN
        verify {
            observabilityManager.enqueue(
                SignupEmailAvailabilityTotal(HttpApiStatus.http5xx),
                any()
            )
        }
    }
}
