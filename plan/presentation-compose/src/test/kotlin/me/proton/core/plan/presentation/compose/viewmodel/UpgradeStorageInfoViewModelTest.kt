package me.proton.core.plan.presentation.compose.viewmodel

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.compose.usecase.ShouldUpgradeStorage
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UpgradeStorageInfoViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var shouldUpgradeStorage: ShouldUpgradeStorage

    private lateinit var storageUsageFlow: MutableStateFlow<ShouldUpgradeStorage.Result>

    private lateinit var tested: UpgradeStorageInfoViewModel

    private val testUserId = UserId("test_user_id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        storageUsageFlow = MutableStateFlow(ShouldUpgradeStorage.Result.NoUpgrade)
        every { shouldUpgradeStorage() } returns storageUsageFlow
        tested = UpgradeStorageInfoViewModel(shouldUpgradeStorage)
    }

    @Test
    fun `storage state`() = coroutinesTest {
        // WHEN
        tested.state.test {
            // THEN
            assertEquals(AccountStorageState.Hidden, awaitItem())

            storageUsageFlow.value = ShouldUpgradeStorage.Result.MailStorageUpgrade(80, testUserId)
            assertEquals(AccountStorageState.HighStorageUsage.Mail(80, testUserId), awaitItem())

            storageUsageFlow.value =
                ShouldUpgradeStorage.Result.DriveStorageUpgrade(100, testUserId)
            assertEquals(AccountStorageState.HighStorageUsage.Drive(100, testUserId), awaitItem())

            storageUsageFlow.value = ShouldUpgradeStorage.Result.NoUpgrade
            assertEquals(AccountStorageState.Hidden, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
