package me.proton.core.auth.presentation.alert.confirmpass

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import me.proton.core.auth.domain.entity.SecondFactorMethod
import me.proton.core.auth.presentation.databinding.DialogConfirmPasswordBinding
import me.proton.core.auth.presentation.viewmodel.ConfirmPasswordDialogViewModel
import me.proton.core.test.android.lifecycle.TestLifecycle
import org.junit.Rule
import kotlin.test.BeforeTest
import kotlin.test.Test

class ConfirmPasswordDialogViewTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    private lateinit var testLifecycle: TestLifecycle
    private lateinit var viewController: ConfirmPasswordDialogViewController

    @BeforeTest
    fun setUp() {
        testLifecycle = TestLifecycle().apply { create() }
        viewController = ConfirmPasswordDialogViewController(
            DialogConfirmPasswordBinding.inflate(paparazzi.layoutInflater),
            lifecycleOwner = testLifecycle,
            onEnterButtonClick = {},
            onCancelButtonClick = {},
            onSecurityKeyInfoClick = {}
        )
    }

    @Test
    fun `idle state`() {
        viewController.setIdle()
        paparazzi.snapshot(viewController.root)
    }

    @Test
    fun `loading state`() {
        viewController.setLoading()
        paparazzi.snapshot(viewController.root)
    }

    @Test
    fun `no second factor methods`() {
        viewController.setSecondFactorResult(
            ConfirmPasswordDialogViewModel.State.SecondFactorResult(
                methods = emptyList()
            )
        )
        paparazzi.snapshot(viewController.root)
    }

    @Test
    fun `totp only`() {
        viewController.setSecondFactorResult(
            ConfirmPasswordDialogViewModel.State.SecondFactorResult(
                methods = listOf(SecondFactorMethod.Totp)
            )
        )
        paparazzi.snapshot(viewController.root)
    }

    @Test
    fun `fido2 only`() {
        viewController.setSecondFactorResult(
            ConfirmPasswordDialogViewModel.State.SecondFactorResult(
                methods = listOf(SecondFactorMethod.Authenticator)
            )
        )
        paparazzi.snapshot(viewController.root)
    }

    @Test
    fun `both 2fa methods - security key selected`() {
        viewController.setSecondFactorResult(
            ConfirmPasswordDialogViewModel.State.SecondFactorResult(
                methods = listOf(SecondFactorMethod.Authenticator, SecondFactorMethod.Totp)
            )
        )
        paparazzi.snapshot(viewController.root)
    }

    @Test
    fun `both 2fa methods - one-time code selected`() {
        viewController.setSecondFactorResult(
            ConfirmPasswordDialogViewModel.State.SecondFactorResult(
                methods = listOf(SecondFactorMethod.Authenticator, SecondFactorMethod.Totp)
            )
        )
        viewController.selectSecondFactorMethodTab(SecondFactorMethod.Totp)
        paparazzi.snapshot(viewController.root)
    }
}
