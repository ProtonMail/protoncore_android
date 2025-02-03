package me.proton.core.paymentiap.test.robot

import android.widget.TextView
import me.proton.test.fusion.Fusion.byObject

public class PlayStoreAccountViewRobot {

    public fun selectPaymentsAndSubscriptionsItem(): PlayStorePaymentsAndSubscriptionsRobot {
        Thread.sleep(3000)
        byObject.withText("Payments & subscriptions")
            .instanceOf(TextView::class.java)
            .waitForExists()
            .click()
        return PlayStorePaymentsAndSubscriptionsRobot()
    }

}