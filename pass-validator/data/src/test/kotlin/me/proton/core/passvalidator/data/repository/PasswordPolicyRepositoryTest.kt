package me.proton.core.passvalidator.data.repository

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import me.proton.core.passvalidator.data.api.PasswordPolicyApi
import me.proton.core.passvalidator.data.entity.PasswordPolicy
import me.proton.core.passvalidator.data.util.makePasswordPolicy
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PasswordPolicyRepositoryTest {
    @MockK
    private lateinit var apiProvider: ApiProvider

    private lateinit var tested: PasswordPolicyRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = PasswordPolicyRepository(apiProvider)
    }

    @Test
    fun `fetching password policies`() = runTest {
        // GIVEN
        val policies1 = listOf(makePasswordPolicy("policy1"))
        coEvery {
            apiProvider.get<PasswordPolicyApi>(userId = null).invoke<List<PasswordPolicy>>(any())
        } returns ApiResult.Success(policies1)

        // WHEN
        tested.observePasswordPolicies(null).test {
            // THEN
            assertEquals(emptyList(), awaitItem())
            assertEquals(policies1, awaitItem())
            awaitComplete()
        }
        coVerify(exactly = 1) { apiProvider.get<PasswordPolicyApi>(userId = null).invoke<List<PasswordPolicy>>(any()) }

        // WHEN
        tested.observePasswordPolicies(null).test {
            // THEN data is loaded from cache
            assertEquals(policies1, awaitItem())
            awaitComplete()
        }
        coVerify(exactly = 1) { apiProvider.get<PasswordPolicyApi>(userId = null).invoke<List<PasswordPolicy>>(any()) }

        // GIVEN
        val policies2 = listOf(makePasswordPolicy("policy2"))
        coEvery {
            apiProvider.get<PasswordPolicyApi>(userId = null).invoke<List<PasswordPolicy>>(any())
        } returns ApiResult.Success(policies2)

        // WHEN
        tested.observePasswordPolicies(null, refresh = true).test {
            // THEN data is loaded from cache, and then refreshed
            assertEquals(policies1, awaitItem())
            assertEquals(policies2, awaitItem())
            awaitComplete()
        }
        coVerify(exactly = 2) { apiProvider.get<PasswordPolicyApi>(userId = null).invoke<List<PasswordPolicy>>(any()) }
    }

    @Test
    fun `cache is per user`() = runTest {
        // GIVEN
        val policiesNoUser = listOf(makePasswordPolicy("policy1"))
        val policiesForUser = listOf(makePasswordPolicy("policy2"))
        val userId = UserId("user-id")
        coEvery {
            apiProvider.get<PasswordPolicyApi>(userId = null).invoke<List<PasswordPolicy>>(any())
        } returns ApiResult.Success(policiesNoUser)
        coEvery {
            apiProvider.get<PasswordPolicyApi>(userId).invoke<List<PasswordPolicy>>(any())
        } returns ApiResult.Success(policiesForUser)

        // WHEN
        tested.observePasswordPolicies(null).test {
            // THEN
            assertEquals(emptyList(), awaitItem())
            assertEquals(policiesNoUser, awaitItem())
            awaitComplete()
        }

        // WHEN
        tested.observePasswordPolicies(userId).test {
            // THEN
            assertEquals(emptyList(), awaitItem())
            assertEquals(policiesForUser, awaitItem())
            awaitComplete()
        }
    }
}