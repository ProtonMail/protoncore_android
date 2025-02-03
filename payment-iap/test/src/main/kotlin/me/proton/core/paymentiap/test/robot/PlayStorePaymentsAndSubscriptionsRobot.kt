package me.proton.core.paymentiap.test.robot

import android.widget.TextView
import me.proton.test.fusion.Fusion.byObject

public class PlayStorePaymentsAndSubscriptionsRobot {

    public fun selectSubscriptionsItem(): PlayStoreSubscriptionsRobot {
        byObject
            .withText("Subscriptions")
            .instanceOf(TextView::class.java)
            .withPkg(playStorePkg)
            .waitForExists()
            .click()
        return PlayStoreSubscriptionsRobot()
    }
}
