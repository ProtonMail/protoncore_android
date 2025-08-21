package me.proton.core.humanverification.test.robot

import android.view.KeyEvent
import android.webkit.WebView
import androidx.test.espresso.web.model.SimpleAtom
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import me.proton.test.fusion.Fusion.device
import kotlin.test.assertNotNull

private const val WEB_VIEW_LOADING_TIMEOUT_MS = 60_000L

/** Corresponds to HV Code WebView. */
public object HvCodeRobot {

    public fun fillCode(): HvCodeRobot = apply {
        waitForWebView()
        repeat(7) { device.pressKeyCode(KeyEvent.KEYCODE_6) }
    }

    public fun fillCodeForInternal(username: String): HvCodeRobot = apply {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val email = "$username@passmail.net"
        val emailTabLocator = "//*[@id=\"label_1\"]"
        val emailFieldLocator = "//*[@id=\"email\"]"
        val setFocusCode = "function(elem) {\nelem.focus();\n}"
        val getCodeLocator = "//button[text()=\"Get verification code\"]"
        val verifyLocator = "//button[text()=\"Verify\"]"
        val code = "666666"

        waitForWebView()
        onWebView()
            .forceJavascriptEnabled()
            .withElement(findElement(Locator.XPATH, emailTabLocator))
            .perform(webClick())
        repeat(2) { device.pressKeyCode(KeyEvent.KEYCODE_TAB) }
        runWithDelay {
            onWebView()
                .withElement(findElement(Locator.XPATH, emailFieldLocator))
                .perform(SimpleAtom(setFocusCode))
        }
        runWithDelay { uiAutomation.executeShellCommand("input text $email") }
        runWithDelay {
            onWebView()
                .withElement(findElement(Locator.XPATH, getCodeLocator)).perform(webClick())
        }
        runWithDelay { uiAutomation.executeShellCommand("input text $code") }
        runWithDelay {
            onWebView().withElement(findElement(Locator.XPATH, verifyLocator)).perform(webClick())
        }
    }

    public fun clickVerify() {
        device.pressEnter()
    }

    public fun close() {
        device.pressBack()
    }

    /** Clicks on the CAPTCHA checkbox. */
    public fun iAmHuman() {
        waitForWebView()
        repeat(4) { device.pressKeyCode(KeyEvent.KEYCODE_TAB) }
        device.pressEnter()
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

    private fun runWithDelay(block: () -> Unit) {
        Thread.sleep(2000)
        block()
    }
}
