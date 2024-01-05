package me.proton.core.auth.test.robot.login

import android.view.KeyEvent
import me.proton.core.auth.presentation.R
import me.proton.test.fusion.Fusion.device
import me.proton.test.fusion.Fusion.view
import me.proton.test.fusion.ui.device.OnDevice
import kotlin.time.Duration.Companion.seconds

/** Corresponds to ProtonWebViewActivity. */
public object IdentityProviderRobot {

    private val webView = view.withId(R.id.webView)

    public fun waitWebViewIsDisplayed(): IdentityProviderRobot = apply {
        webView.await(interval = 1.seconds, timeout = 60.seconds) { checkIsDisplayed() }
    }

    public fun fillAuth(): IdentityProviderRobot = apply {
        device.pressKeyCode(KeyEvent.KEYCODE_TAB)
        device.pressKeyCodes("tobbe".toKeyCodes())
        device.pressKeyCode(KeyEvent.KEYCODE_AT)
        device.pressKeyCodes("ssotest".toKeyCodes())
        device.pressKeyCode(KeyEvent.KEYCODE_PERIOD)
        device.pressKeyCodes("protonhub".toKeyCodes())
        device.pressKeyCode(KeyEvent.KEYCODE_PERIOD)
        device.pressKeyCodes("org".toKeyCodes())
        device.pressKeyCode(KeyEvent.KEYCODE_TAB)
        device.pressKeyCodes("password".toKeyCodes())
        device.pressKeyCode(KeyEvent.KEYCODE_6)
        device.pressKeyCode(KeyEvent.KEYCODE_6)
        device.pressKeyCode(KeyEvent.KEYCODE_6)
    }

    private const val aCode = 'a'.code
    private const val deltaAsciiKeyCode = aCode - KeyEvent.KEYCODE_A
    private fun String.toKeyCodes() = map { it.code - deltaAsciiKeyCode }
    private fun OnDevice.pressKeyCodes(codes: List<Int>) = codes.forEach { pressKeyCode(it) }

    public fun clickVerify() {
        device.pressKeyCode(KeyEvent.KEYCODE_TAB)
        device.pressEnter()
    }
}
