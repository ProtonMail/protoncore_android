package me.proton.core.devicemigration.presentation.codeinput

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCodeInputViewModelTest: CoroutinesTest by CoroutinesTest() {
    private lateinit var tested: ManualCodeInputViewModel

    @BeforeTest
    fun setUp() {
        tested = ManualCodeInputViewModel()
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
}
