package me.proton.core.passvalidator.data.usecase

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.type.IntEnum
import me.proton.core.passvalidator.data.api.response.toPasswordPolicyState
import me.proton.core.passvalidator.data.feature.IsPasswordPolicyEnabled
import me.proton.core.passvalidator.data.repository.PasswordPolicyRepository
import me.proton.core.passvalidator.data.util.makePasswordPolicy
import me.proton.core.passvalidator.data.validator.PasswordPolicyValidator
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

class ObservePasswordPolicyValidatorsTest {

    @MockK
    private lateinit var isPasswordPolicyEnabled: IsPasswordPolicyEnabled

    @MockK
    private lateinit var passwordPolicyRepository: PasswordPolicyRepository

    private lateinit var tested: ObservePasswordPolicyValidators

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ObservePasswordPolicyValidators(isPasswordPolicyEnabled, passwordPolicyRepository)
    }

    @Test
    fun `password policy flag is disabled`() = runTest {
        // GIVEN
        every { isPasswordPolicyEnabled(any()) } returns false

        // WHEN
        tested(null).test {
            // THEN
            assertEquals(emptyList(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `password policy flag is enabled`() = runTest {
        // GIVEN
        val disabledPolicy = makePasswordPolicy("policy0", state = IntEnum(0, 0.toPasswordPolicyState()))
        val enabledPolicy = makePasswordPolicy("policy1", state = IntEnum(1, 1.toPasswordPolicyState()))

        every { isPasswordPolicyEnabled(any()) } returns true
        every { passwordPolicyRepository.observePasswordPolicies(any()) } returns flowOf(
            listOf(disabledPolicy, enabledPolicy)
        )

        // WHEN
        tested(null).test {
            // THEN
            awaitItem().let { validators ->
                val firstValidator = validators.first()
                assertEquals(1, validators.size)
                assertIs<PasswordPolicyValidator>(firstValidator)
                assertEquals(enabledPolicy, firstValidator.policy)
            }
            awaitComplete()
        }
    }

    @Test
    fun `rethrows error on failure`() = runTest {
        // GIVEN
        every { isPasswordPolicyEnabled(any()) } returns true
        every { passwordPolicyRepository.observePasswordPolicies(any()) } returns flow {
            error("Cannot fetch")
        }

        // WHEN
        tested(null).test {
            // THEN
            assertFails("Cannot fetch") { awaitItem() }
        }
    }
}
