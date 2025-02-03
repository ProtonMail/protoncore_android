package me.proton.core.paymentiap.test.robot

import android.content.Context
import android.content.Intent
import android.widget.FrameLayout
import androidx.test.platform.app.InstrumentationRegistry
import me.proton.test.fusion.Fusion.byObject

public class PlayStoreHomeRobot {

    public fun clickOnAccountButton(): PlayStoreAccountViewRobot {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageName = "com.android.vending"

        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        requireNotNull(intent) { "Google Play Store not found! Is it installed on the emulator?" }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)

        byObject.withContentDescContains("Signed in as")
            .instanceOf(FrameLayout::class.java)
            .waitForExists()
            .click()
        return PlayStoreAccountViewRobot()
    }
}