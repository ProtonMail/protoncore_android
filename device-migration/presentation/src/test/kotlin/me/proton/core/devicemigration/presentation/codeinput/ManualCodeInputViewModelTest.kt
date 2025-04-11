package me.proton.core.devicemigration.presentation.codeinput

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.devicemigration.domain.usecase.DecodeEdmCode
import me.proton.core.devicemigration.domain.usecase.PushEdmSessionFork
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCodeInputViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var decodeEdmCode: DecodeEdmCode

    @MockK
    private lateinit var observabilityManager: ObservabilityManager

    @MockK
    private lateinit var pushEdmSessionFork: PushEdmSessionFork

    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var tested: ManualCodeInputViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ManualCodeInputViewModel(
            context = context,
            decodeEdmCode = decodeEdmCode,
            observabilityManager = observabilityManager,
            pushEdmSessionFork = pushEdmSessionFork,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun `submit empty code`() = runTest {
        tested.state.test {
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Loading), awaitItem())
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Idle), awaitItem())

            // WHEN
            tested.perform(ManualCodeInputAction.Submit(""))

            // THEN
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Error.EmptyCode), awaitItem())
        }
    }

    @Test
    fun `submit invalid code`() = runTest {
        coEvery { decodeEdmCode(any()) } returns null

        tested.state.test {
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Loading), awaitItem())
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Idle), awaitItem())

            // WHEN
            tested.perform(ManualCodeInputAction.Submit("invalid"))

            // THEN
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Loading), awaitItem())
            assertEquals(ManualCodeInputStateHolder(state = ManualCodeInputState.Error.InvalidCode), awaitItem())
        }
    }
}
