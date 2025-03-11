package me.proton.core.devicemigration.presentation.success

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import me.proton.core.devicemigration.presentation.DeviceMigrationRoutes.Arg.KEY_USER_ID
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.UserManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OriginSuccessViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK(relaxed = true)
    private lateinit var context: Context

    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    @MockK
    private lateinit var userManager: UserManager

    private lateinit var tested: OriginSuccessViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = OriginSuccessViewModel(context, savedStateHandle, userManager)
    }

    @Test
    fun `loading user email`() = coroutinesTest {
        // GIVEN
        every { savedStateHandle.get<String>(KEY_USER_ID) } returns "user-id"
        coEvery { userManager.getUser(any()) } coAnswers {
            yield()
            mockk { every { email } returns "email" }
        }

        tested.state.test {
            // THEN
            assertEquals(OriginSuccessState.Loading, awaitItem())
            assertEquals(OriginSuccessState.Idle("email"), awaitItem())
        }
    }

    @Test
    fun `failure while loading`() = coroutinesTest {
        // GIVEN
        every { savedStateHandle.get<String>(KEY_USER_ID) } returns "user-id"
        coEvery { userManager.getUser(any()) } coAnswers {
            yield()
            error("Error")
        }

        tested.state.test {
            // THEN
            assertEquals(OriginSuccessState.Loading, awaitItem())
            assertEquals(OriginSuccessState.Error.Unknown("Error"), awaitItem())
        }
    }
}
