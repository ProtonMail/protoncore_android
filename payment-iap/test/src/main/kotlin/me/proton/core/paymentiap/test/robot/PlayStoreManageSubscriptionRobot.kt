package me.proton.core.paymentiap.test.robot

import android.view.View
import android.widget.TextView
import me.proton.test.fusion.Fusion.byObject

public class PlayStoreManageSubscriptionRobot {

    public fun clickCancelSubscription(): PlayStorePauseSubscriptionBottomSheetRobot {
        byObject
            .instanceOf(TextView::class.java)
            .withText("Cancel subscription")
            .waitForExists()
            .click()
        return PlayStorePauseSubscriptionBottomSheetRobot()
    }

    public fun subscriptionIsCancelled(): PlayStoreManageSubscriptionRobot {
        byObject
            .instanceOf(TextView::class.java)
            .withPkg(playStorePkg)
            .withText("Canceled")
            .waitForExists()
            .checkExists()
        return PlayStoreManageSubscriptionRobot()
    }

    public fun clickPausePayments(): GPBottomSheetPaymentMethodsRobot {
        byObject
            .instanceOf(TextView::class.java)
            .withPkg(playStorePkg)
            .withText("Pause payments")
            .withContentDesc("Pause payments")
            .waitForExists()
            .click()
        return GPBottomSheetPaymentMethodsRobot()
    }

    public fun clickUpdatePrimaryPaymentMethod(): GPBottomSheetPaymentMethodsRobot {
        byObject
            .instanceOf(View::class.java)
            .withPkg(playStorePkg)
            .hasDescendant(
                byObject.containsText("Update").instanceOf(TextView::class.java)
                    .withPkg(playStorePkg)
            )
            .click()
        return GPBottomSheetPaymentMethodsRobot()
    }
}