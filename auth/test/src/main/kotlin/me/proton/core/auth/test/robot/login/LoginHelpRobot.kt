package me.proton.core.auth.test.robot.login

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import me.proton.core.auth.presentation.R
import me.proton.test.fusion.Fusion.intent
import me.proton.test.fusion.Fusion.view

/** Requires [androidx.test.espresso.intent.rule.IntentsRule]. */
public object LoginHelpRobot {
    private val forgotUsernameButton = view.withId(R.id.helpOptionForgotUsername)
    private val forgotPasswordButton = view.withId(R.id.helpOptionForgotPassword)
    private val otherIssuesButton = view.withId(R.id.helpOptionOtherIssues)
    private val customerSupportButton = view.withId(R.id.helpOptionCustomerSupport)

    private const val forgotUsernameUrl = "https://account.proton.me/forgot-username"
    private const val forgotPasswordUrl = "https://account.proton.me/reset-password"
    private const val otherIssuesUrl = "https://proton.me/support/common-login-problems"

    public fun forgotUsername(): LoginHelpRobot = apply {
        mockBrowserIntent(forgotUsernameUrl)
        forgotUsernameButton.click()
    }

    public fun forgotPassword(): LoginHelpRobot = apply {
        mockBrowserIntent(forgotPasswordUrl)
        forgotPasswordButton.click()
    }

    public fun otherIssues(): LoginHelpRobot = apply {
        mockBrowserIntent(otherIssuesUrl)
        otherIssuesButton.click()
    }

    public fun customerSupport() {
        customerSupportButton.click()
    }

    public fun forgotUsernameBrowserOpened() {
        intent.checkBrowserOpened(forgotUsernameUrl)
    }

    public fun forgotPasswordBrowserOpened() {
        intent.checkBrowserOpened(forgotPasswordUrl)
    }

    public fun otherIssuesBrowserOpened() {
        intent.checkBrowserOpened(otherIssuesUrl)
    }

    private fun mockBrowserIntent(forUrl: String) {
        intent
            .hasAction(Intent.ACTION_VIEW)
            .hasDataUri(Uri.parse(forUrl))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))
    }
}
