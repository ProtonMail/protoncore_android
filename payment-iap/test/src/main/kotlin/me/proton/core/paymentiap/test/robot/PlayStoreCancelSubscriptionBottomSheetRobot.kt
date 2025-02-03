package me.proton.core.paymentiap.test.robot

import android.widget.TextView
import me.proton.test.fusion.Fusion.byObject

public class PlayStoreCancelSubscriptionBottomSheetRobot {

    public fun clickCancelSubscription(): PlayStoreManageSubscriptionRobot {
        byObject
            .instanceOf(TextView::class.java)
            .withPkg(playStorePkg)
            .withText("Cancel subscription")
            .waitForExists()
            .click()
        return PlayStoreManageSubscriptionRobot()
    }
}