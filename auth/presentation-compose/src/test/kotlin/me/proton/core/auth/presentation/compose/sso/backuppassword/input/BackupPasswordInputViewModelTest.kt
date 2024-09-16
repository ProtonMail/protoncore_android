package me.proton.core.auth.presentation.compose.sso.backuppassword.input

import app.cash.turbine.test
import me.proton.core.auth.presentation.compose.R
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class BackupPasswordInputViewModelTest : CoroutinesTest by CoroutinesTest() {

    private lateinit var tested: BackupPasswordInputViewModel

    @BeforeTest
    fun setUp() {
        // TODO: tested = BackupPasswordInputViewModel()
    }

    //TODO: @Test
    fun `empty password`() = coroutinesTest {
        tested.state.test {
            assertEquals(BackupPasswordInputState.Idle, awaitItem())

            // WHEN
            tested.submit(BackupPasswordInputAction.Submit(""))

            // THEN
            assertEquals(BackupPasswordInputState.Loading, awaitItem())
            assertEquals(
                BackupPasswordInputState.FormError(R.string.backup_password_input_password_empty),
                awaitItem()
            )
        }
    }
}
