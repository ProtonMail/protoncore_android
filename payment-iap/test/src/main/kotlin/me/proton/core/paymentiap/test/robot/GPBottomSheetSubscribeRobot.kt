package me.proton.core.paymentiap.test.robot

import android.widget.Button
import android.widget.LinearLayout
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiWatcher
import me.proton.core.test.android.instrumented.FusionConfig
import me.proton.test.fusion.Fusion.byObject

public class GPBottomSheetSubscribeRobot {

    private val device: UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    public fun registerPlayPointsNotNowButtonWatcher() {
        device.registerWatcher("Google play Points Not now button watcher", UiWatcher {
            val registerButton = device.findObject(By.text("Not now").clazz(Button::class.java))
            if (registerButton != null && registerButton.isEnabled) {
                println("'Google play Points Not now' button detected! Clicking it now...")
                registerButton.click()
                return@UiWatcher true
            }
            false
        })
        device.runWatchers()
    }

    public fun openPaymentMethods(): GPBottomSheetPaymentMethodsRobot {
        FusionConfig.uiAutomator.boost()
        byObject
            .withContentDescContains("Change payment method")
            .withPkg(playStorePkg)
            .isClickable()
            .instanceOf(LinearLayout::class.java)
            .waitForExists()
            .click()
        return GPBottomSheetPaymentMethodsRobot()
    }

    public inline fun <reified T> clickSubscribeButton(): T {
        FusionConfig.uiAutomator.boost()
        byObject.withText("Subscribe")
            .withPkg(playStorePkg)
            .instanceOf(Button::class.java)
            .isClickable()
            .waitForExists()
            .click()

        registerPlayPointsNotNowButtonWatcher()
        return T::class.java.getDeclaredConstructor().newInstance()
    }
}