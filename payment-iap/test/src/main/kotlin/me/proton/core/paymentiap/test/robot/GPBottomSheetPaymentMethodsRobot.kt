package me.proton.core.paymentiap.test.robot

import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import me.proton.test.fusion.Fusion.byObject

public class GPBottomSheetPaymentMethodsRobot {

    public val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())


    public inline fun <reified T> selectAlwaysApproves(): T {
        selectCardItem(testCardAlwaysApprovesText)
        return T::class.java.getDeclaredConstructor().newInstance()
    }

    public inline fun <reified T> selectAlwaysDeclines(): T {
        selectCardItem(testCardAlwaysDeclinesText)
        return T::class.java.getDeclaredConstructor().newInstance()
    }

    public fun selectCardItem(cardText: String) {
        InstrumentationRegistry
            .getInstrumentation()
            .uiAutomation
            .waitForIdle(3_000L, 10_000L)
        byObject
            .withText(cardText)
            .instanceOf(TextView::class.java)
            .waitForExists()
            .checkExists()
            .click()
    }
}
