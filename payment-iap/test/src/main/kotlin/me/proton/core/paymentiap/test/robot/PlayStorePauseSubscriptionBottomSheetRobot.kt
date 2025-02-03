package me.proton.core.paymentiap.test.robot

import android.widget.TextView
import me.proton.test.fusion.Fusion.byObject

public class PlayStorePauseSubscriptionBottomSheetRobot {

    public fun clickNoThanksButton(isMonthlyBillingCycle: Boolean): PlayStoreWhatsMakingYouCancelRobot {
        // Only trigger for monthly billing cycle
        if (isMonthlyBillingCycle) {
            byObject
                .instanceOf(TextView::class.java)
                .withPkg(playStorePkg)
                .withText("No thanks")
                .waitForExists()
                .click()
        }
        return PlayStoreWhatsMakingYouCancelRobot()
    }
}