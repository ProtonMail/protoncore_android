package me.proton.core.auth.test.robot.login

import me.proton.test.fusion.Fusion

/** Corresponds to IdentityProvider WebView. */
public object IdentityProviderRobot {

    public fun fillAuth(): IdentityProviderRobot = apply {
        waitForWebView()
        TODO("Authenticate to IDP.")
    }

    public fun clickVerify() {
        Fusion.device.pressEnter()
    }

    private fun waitForWebView() {
        TODO("Implement waitForWebView.")
    }
}