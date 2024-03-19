package me.proton.core.test.android.instrumented.matchers

import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.view.WindowManager.LayoutParams.TYPE_TOAST
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ToastOverlayMatcher(
    private val matcherDescription: String = "WindowOverlayMatcher"
) : TypeSafeMatcher<Root>() {

    override fun describeTo(description: Description) {
        description.appendText(matcherDescription)
    }

    override fun matchesSafely(root: Root): Boolean {
        val type = root.windowLayoutParams.get().type
        if (type in listOf(TYPE_TOAST, TYPE_APPLICATION_OVERLAY)) {
            val windowToken = root.decorView.windowToken
            val appToken = root.decorView.applicationWindowToken
            return windowToken == appToken
        }
        return false
    }
}
