package me.proton.core.auth.test.robot

import me.proton.core.auth.presentation.R
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.test.fusion.Fusion.view

/** Corresponds to [me.proton.core.auth.presentation.ui.LoginActivity]. */
public object LoginRobot {
    private val usernameInput = view.withCustomMatcher(inputFieldMatcher(R.id.usernameInput))
    private val passwordInput = view.withCustomMatcher(inputFieldMatcher(R.id.passwordInput))
    private val signInButton = view.withId(R.id.signInButton)

    public fun fillUsername(username: String): LoginRobot = apply {
        usernameInput.typeText(username)
    }

    public fun fillPassword(password: String): LoginRobot = apply {
        passwordInput.typeText(password)
    }

    public fun login() {
        signInButton.click()
    }
}
