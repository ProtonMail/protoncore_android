package me.proton.core.paymentiap.test.robot

import android.widget.Button
import android.widget.TextView
import me.proton.core.test.android.instrumented.FusionConfig
import me.proton.test.fusion.Fusion.byObject

public class GPBottomSheetSubscribeErrorRobot {

    public inline fun <reified T> clickGotIt(): T {
        FusionConfig.uiAutomator.boost()
        byObject
            .withText("Got it")
            .withPkg(playStorePkg)
            .isClickable()
            .instanceOf(Button::class.java)
            .waitForExists()
            .click()
        return T::class.java.getDeclaredConstructor().newInstance()
    }

    public fun errorMessageIsShown(): GPBottomSheetSubscribeErrorRobot {
        FusionConfig.uiAutomator.boost()
        byObject
            .withPkg(playStorePkg)
            .instanceOf(TextView::class.java)
            .withText("Error")
            .waitForExists()
            .checkExists()

        byObject
            .withPkg(playStorePkg)
            .instanceOf(TextView::class.java)
            .withText("Declined by always denied test instrument")
            .waitForExists()
            .checkExists()

        return GPBottomSheetSubscribeErrorRobot()
    }
}
