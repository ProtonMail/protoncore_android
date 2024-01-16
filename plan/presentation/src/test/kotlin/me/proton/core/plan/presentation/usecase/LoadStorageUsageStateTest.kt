package me.proton.core.plan.presentation.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.IsSplitStorageEnabled
import me.proton.core.user.domain.usecase.GetUser
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadStorageUsageStateTest {
    @MockK
    private lateinit var getUser: GetUser

    @MockK
    private lateinit var isSplitStorageEnabled: IsSplitStorageEnabled

    private lateinit var tested: LoadStorageUsageState

    private val testUserId = UserId("test_user_id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = LoadStorageUsageState(getUser, isSplitStorageEnabled)
    }

    @Test
    fun `if split storage is disabled, then returns null`() = runTest {
        every { isSplitStorageEnabled(any()) } returns false
        assertEquals(null, tested(testUserId))
    }

    @Test
    fun `if user has a subscription, then returns null`() = runTest {
        // GIVEN
        every { isSplitStorageEnabled(any()) } returns true
        coEvery { getUser(any(), any()) } returns mockk {
            every { subscribed } returns 1
        }

        // THEN
        assertEquals(null, tested(testUserId))
    }

    @Test
    fun `drive storage is nearly full`() = runTest {
        // GIVEN
        every { isSplitStorageEnabled(any()) } returns true
        coEvery { getUser(any(), any()) } returns mockk {
            every { subscribed } returns 0

            every { usedBaseSpace } returns null
            every { maxBaseSpace } returns null

            every { usedDriveSpace } returns 80
            every { maxDriveSpace } returns 100
        }

        // THEN
        assertEquals(StorageUsageState.NearlyFull(Product.Drive), tested(testUserId))
    }

    @Test
    fun `drive storage is full`() = runTest {
        // GIVEN
        every { isSplitStorageEnabled(any()) } returns true
        coEvery { getUser(any(), any()) } returns mockk {
            every { subscribed } returns 0

            every { usedBaseSpace } returns 30
            every { maxBaseSpace } returns 100

            every { usedDriveSpace } returns 100
            every { maxDriveSpace } returns 100
        }

        // THEN
        assertEquals(StorageUsageState.Full(Product.Drive), tested(testUserId))
    }

    @Test
    fun `mail storage is nearly full`() = runTest {
        // GIVEN
        every { isSplitStorageEnabled(any()) } returns true
        coEvery { getUser(any(), any()) } returns mockk {
            every { subscribed } returns 0

            every { usedBaseSpace } returns 80
            every { maxBaseSpace } returns 100

            every { usedDriveSpace } returns null
            every { maxDriveSpace } returns null
        }

        // THEN
        assertEquals(StorageUsageState.NearlyFull(Product.Mail), tested(testUserId))
    }

    @Test
    fun `mail storage is full`() = runTest {
        // GIVEN
        every { isSplitStorageEnabled(any()) } returns true
        coEvery { getUser(any(), any()) } returns mockk {
            every { subscribed } returns 0

            every { usedBaseSpace } returns 100
            every { maxBaseSpace } returns 100

            every { usedDriveSpace } returns 30
            every { maxDriveSpace } returns 100
        }

        // THEN
        assertEquals(StorageUsageState.Full(Product.Mail), tested(testUserId))
    }
}
