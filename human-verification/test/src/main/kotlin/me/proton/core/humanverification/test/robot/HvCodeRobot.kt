package me.proton.core.humanverification.test.robot

import android.view.KeyEvent
import android.webkit.WebView
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import me.proton.test.fusion.Fusion
import kotlin.test.assertNotNull

private const val WEB_VIEW_LOADING_TIMEOUT_MS = 30_000L

/** Corresponds to HV Code WebView. */
public object HvCodeRobot {

    public fun fillCode(): HvCodeRobot = apply {
        waitForWebView()
        repeat(7) { Fusion.device.pressKeyCode(KeyEvent.KEYCODE_6) }
    }

    public fun clickVerify() {
        Fusion.device.pressEnter()
    }

    public fun close() {
        Fusion.device.pressBack()
    }

    /** Clicks on the CAPTCHA checkbox. */
    public fun iAmHuman() {
        waitForWebView()
        repeat(4) { Fusion.device.pressKeyCode(KeyEvent.KEYCODE_TAB) }
        Fusion.device.pressEnter()
    }

    public fun waitForWebView() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val result = uiDevice.wait(
            Until.findObject(By.clazz(WebView::class.java)), WEB_VIEW_LOADING_TIMEOUT_MS
        )
        assertNotNull(
            result,
            "Expected human verification WebView to be visible but it wasn't " +
                    "within given timeout: ${WEB_VIEW_LOADING_TIMEOUT_MS / 1000} seconds"
        )
    }
}
