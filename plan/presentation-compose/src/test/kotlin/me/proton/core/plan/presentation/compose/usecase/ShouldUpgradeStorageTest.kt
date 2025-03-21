package me.proton.core.plan.presentation.compose.usecase

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.IsSplitStorageEnabled
import me.proton.core.plan.domain.usecase.CanUpgradeFromMobile
import me.proton.core.plan.presentation.compose.usecase.ShouldUpgradeStorage.Result.DriveStorageUpgrade
import me.proton.core.plan.presentation.compose.usecase.ShouldUpgradeStorage.Result.MailStorageUpgrade
import me.proton.core.plan.presentation.compose.usecase.ShouldUpgradeStorage.Result.NoUpgrade
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ShouldUpgradeStorageTest {
    @MockK
    private lateinit var accountManager: AccountManager

    @MockK
    private lateinit var canUpgradeFromMobile: CanUpgradeFromMobile

    @MockK
    private lateinit var isSplitStorageEnabled: IsSplitStorageEnabled

    @MockK
    private lateinit var observeStorageUsage: ObserveStorageUsage

    private lateinit var primaryUserIdFlow: MutableStateFlow<UserId?>
    private lateinit var storageState: MutableStateFlow<ObserveStorageUsage.StorageUsage?>

    private lateinit var scopeProvider: CoroutineScopeProvider
    private lateinit var tested: ShouldUpgradeStorage

    private val testUserId = UserId("test_user_id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        primaryUserIdFlow = MutableStateFlow(testUserId)
        every { accountManager.getPrimaryUserId() } returns primaryUserIdFlow

        storageState = MutableStateFlow(null)
        every { observeStorageUsage(any()) } returns storageState

        scopeProvider = TestCoroutineScopeProvider(TestDispatcherProvider(UnconfinedTestDispatcher()))
        tested = ShouldUpgradeStorage(
            accountManager,
            canUpgradeFromMobile,
            isSplitStorageEnabled,
            observeStorageUsage,
            scopeProvider
        )
    }

    @Test
    fun `cannot upgrade from mobile`() = runTest {
        // GIVEN
        coEvery { canUpgradeFromMobile(any()) } returns false
        every { isSplitStorageEnabled(any()) } returns true

        // WHEN
        tested().test {
            // THEN
            assertEquals(NoUpgrade, awaitItem())

            emitStorageState(40, 85, 40)
            expectNoEvents() // state is still NoUpgrade
        }
    }

    @Test
    fun `split storage disabled`() = runTest {
        // GIVEN
        coEvery { canUpgradeFromMobile(any()) } returns true
        every { isSplitStorageEnabled(any()) } returns false

        // WHEN
        tested().test {
            // THEN
            assertEquals(NoUpgrade, awaitItem())

            emitStorageState(90, 20, 30)
            expectNoEvents() // state is still NoUpgrade
        }
    }

    @Test
    fun `storage is still available`() = runTest {
        // GIVEN
        coEvery { canUpgradeFromMobile(any()) } returns true
        every { isSplitStorageEnabled(any()) } returns true

        // WHEN
        tested().test {
            // THEN
            assertEquals(NoUpgrade, awaitItem())

            emitStorageState(10, 30, 15)
            expectNoEvents() // state is still NoUpgrade
        }
    }

    @Test
    fun `base storage is not available`() = runTest {
        // GIVEN
        coEvery { canUpgradeFromMobile(any()) } returns true
        every { isSplitStorageEnabled(any()) } returns true

        // WHEN
        tested().test {
            // THEN
            assertEquals(NoUpgrade, awaitItem())

            emitStorageState(80, 30, 50)
            assertEquals(
                MailStorageUpgrade(
                    storagePercentage = 80,
                    testUserId
                ), awaitItem()
            )
        }
    }

    @Test
    fun `drive storage is not available`() = runTest {
        // GIVEN
        coEvery { canUpgradeFromMobile(any()) } returns true
        every { isSplitStorageEnabled(any()) } returns true

        // WHEN
        tested().test {
            // THEN
            assertEquals(NoUpgrade, awaitItem())

            emitStorageState(30, 81, 50)
            assertEquals(
                DriveStorageUpgrade(
                    storagePercentage = 81,
                    testUserId
                ), awaitItem()
            )
        }
    }

    @Test
    fun `no primary user`() = runTest {
        // GIVEN
        coEvery { canUpgradeFromMobile(any()) } returns true
        every { isSplitStorageEnabled(any()) } returns true

        // WHEN
        tested().test {
            // THEN
            assertEquals(NoUpgrade, awaitItem())

            emitStorageState(80, 81, 40)
            assertEquals(
                MailStorageUpgrade(80, testUserId),
                awaitItem()
            )

            primaryUserIdFlow.value = null
            assertEquals(NoUpgrade, awaitItem())
        }
    }

    private fun emitStorageState(basePercentage: Int, drivePercentage: Int, totalPercentage: Int) {
        storageState.value = ObserveStorageUsage.StorageUsage(
            basePercentage = basePercentage,
            drivePercentage = drivePercentage,
            totalPercentage = totalPercentage,
            userId = testUserId
        )
    }
}
