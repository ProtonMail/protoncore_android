package me.proton.core.plan.presentation.compose.usecase

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveStorageUsageTest {
    @MockK
    lateinit var userManager: UserManager

    private lateinit var tested: ObserveStorageUsage

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ObserveStorageUsage(userManager)
    }

    @Test
    fun `no user`() = runTest {
        // GIVEN
        coEvery { userManager.observeUser(any()) } returns MutableStateFlow(null)

        // WHEN
        tested(UserId("test_user_id")).test {
            // THEN
            assertNull(awaitItem())
        }
    }

    @Test
    fun `user with split storage`() = runTest {
        // GIVEN
        val testUserId = UserId("test_user_id")
        coEvery { userManager.observeUser(any()) } returns MutableStateFlow(mockk {
            every { userId } returns testUserId
            every { maxSpace } returns 300
            every { usedSpace } returns 60
            every { maxBaseSpace } returns 100
            every { usedBaseSpace } returns 10
            every { maxDriveSpace } returns 200
            every { usedDriveSpace } returns 50
        })

        // WHEN
        tested(testUserId).test {
            // THEN
            assertEquals(
                ObserveStorageUsage.StorageUsage(
                    basePercentage = 10,
                    drivePercentage = 25,
                    totalPercentage = 20,
                    testUserId
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `user without split storage`() = runTest {
        // GIVEN
        val testUserId = UserId("test_user_id")
        coEvery { userManager.observeUser(any()) } returns MutableStateFlow(mockk {
            every { userId } returns testUserId
            every { maxSpace } returns 100
            every { usedSpace } returns 0
            every { maxBaseSpace } returns null
            every { usedBaseSpace } returns null
            every { maxDriveSpace } returns null
            every { usedDriveSpace } returns null
        })

        // WHEN
        tested(testUserId).test {
            // THEN
            assertEquals(
                ObserveStorageUsage.StorageUsage(
                    basePercentage = null,
                    drivePercentage = null,
                    totalPercentage = 0,
                    testUserId
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `storage is updated`() = runTest {
        // GIVEN
        val testUserId = UserId("test_user_id")
        val testUser = mockk<User> {
            every { userId } returns testUserId
            every { maxSpace } returns 200
            every { usedSpace } returns 130
            every { maxBaseSpace } returns 100
            every { usedBaseSpace } returns 80
            every { maxDriveSpace } returns 100
            every { usedDriveSpace } returns 50
        }
        val userFlow = MutableStateFlow(testUser)
        coEvery { userManager.observeUser(any()) } returns userFlow

        // WHEN
        tested(testUserId).test {
            // THEN
            assertEquals(
                ObserveStorageUsage.StorageUsage(
                    basePercentage = 80,
                    drivePercentage = 50,
                    totalPercentage = 65,
                    testUserId
                ),
                awaitItem()
            )

            // WHEN
            userFlow.value = mockk<User> {
                every { userId } returns testUserId
                every { maxSpace } returns 200
                every { usedSpace } returns 100
                every { maxBaseSpace } returns 100
                every { usedBaseSpace } returns 10
                every { maxDriveSpace } returns 100
                every { usedDriveSpace } returns 90
            }

            // THEN
            assertEquals(
                ObserveStorageUsage.StorageUsage(
                    basePercentage = 10,
                    drivePercentage = 90,
                    totalPercentage = 50,
                    testUserId
                ),
                awaitItem()
            )
        }
    }
}
