package me.proton.core.devicemigration.presentation.settings

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.devicemigration.domain.usecase.IsEasyDeviceMigrationAvailable
import me.proton.core.domain.entity.UserId
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SignInToAnotherDeviceViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var accountManager: AccountManager

    @MockK
    private lateinit var isEasyDeviceMigrationAvailable: IsEasyDeviceMigrationAvailable
    private lateinit var tested: SignInToAnotherDeviceViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = SignInToAnotherDeviceViewModel(
            accountManager = accountManager,
            isEasyDeviceMigrationAvailable = isEasyDeviceMigrationAvailable
        )
    }

    @Test
    fun `no primary user`() = runTest {
        // GIVEN
        every { accountManager.getPrimaryUserId() } returns flowOf(null)

        tested.state.test {
            // THEN
            assertEquals(SignInToAnotherDeviceState.Hidden, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `edm disabled via user setting`() = runTest {
        // GIVEN
        every { accountManager.getPrimaryUserId() } returns flowOf(UserId("user-id"))
        coEvery { isEasyDeviceMigrationAvailable(any()) } returns false

        tested.state.test {
            // THEN
            assertEquals(SignInToAnotherDeviceState.Hidden, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `edm available`() = runTest {
        // GIVEN
        every { accountManager.getPrimaryUserId() } returns flowOf(UserId("user-id"))
        coEvery { isEasyDeviceMigrationAvailable(any()) } returns true

        tested.state.test {
            // THEN
            assertEquals(SignInToAnotherDeviceState.Hidden, awaitItem())
            assertEquals(SignInToAnotherDeviceState.Visible(UserId("user-id")), awaitItem())
            expectNoEvents()
        }
    }
}
