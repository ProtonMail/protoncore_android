package me.proton.core.paymentiap.test.robot

import android.widget.RadioButton
import android.widget.TextView
import me.proton.test.fusion.Fusion.byObject

public class PlayStoreWhatsMakingYouCancelRobot {

    public fun clickIDontUseServiceEnoughItemAndPressContinue(): PlayStoreCancelSubscriptionBottomSheetRobot {
        byObject
            .instanceOf(RadioButton::class.java)
            .withContentDesc("I don't use this service enough")
            .waitForExists()
            .click()
        byObject
            .instanceOf(TextView::class.java)
            .withPkg(playStorePkg)
            .withText("Continue")
            .waitForExists()
            .click()
        return PlayStoreCancelSubscriptionBottomSheetRobot()
    }
}