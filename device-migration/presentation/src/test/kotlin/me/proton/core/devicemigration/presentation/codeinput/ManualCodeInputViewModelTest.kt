package me.proton.core.devicemigration.presentation.codeinput

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.devicemigration.domain.usecase.DecodeEdmCode
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCodeInputViewModelTest: CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var decodeEdmCode: DecodeEdmCode
    private lateinit var tested: ManualCodeInputViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ManualCodeInputViewModel(decodeEdmCode)
    }

    @Test
    fun `submit empty code`() = runTest {
        tested.state.test {
            assertEquals(ManualCodeInputState.Idle, awaitItem())

            // WHEN
            tested.perform(ManualCodeInputAction.Submit(""))

            // THEN
            assertEquals(ManualCodeInputState.Error.EmptyCode, awaitItem())
        }
    }

    @Test
    fun `submit invalid code`() = runTest {
        every { decodeEdmCode(any()) } returns null

        tested.state.test {
            assertEquals(ManualCodeInputState.Idle, awaitItem())

            // WHEN
            tested.perform(ManualCodeInputAction.Submit("invalid"))

            // THEN
            assertEquals(ManualCodeInputState.Loading, awaitItem())
            assertEquals(ManualCodeInputState.Error.InvalidCode, awaitItem())
        }
    }
}
