package me.proton.core.paymentiap.test.robot

import android.widget.TextView
import me.proton.test.fusion.Fusion.byObject

public class PlayStoreSubscriptionsRobot {

    public fun clickActiveSubscriptionByText(planName: String): PlayStoreManageSubscriptionRobot {
        byObject
            // We need a new line here as we can match 1 month plan name
            .containsText("$planName\n")
            .instanceOf(TextView::class.java)
            .withPkg(playStorePkg)
            .waitForExists()
            .click()
        return PlayStoreManageSubscriptionRobot()
    }
}