package me.proton.core.auth.test.robot

import me.proton.core.auth.presentation.R
import me.proton.test.fusion.Fusion.view

/** Corresponds to [me.proton.core.auth.presentation.ui.AddAccountActivity]. */
public object AddAccountRobot {
    private val signInButton = view.withId(R.id.sign_in)
    private val signUpButton = view.withId(R.id.sign_up)

    public fun goToLogin(): LoginRobot {
        signInButton.click()
        return LoginRobot
    }

    public fun uiElementsDisplayed() {
        signInButton.checkIsDisplayed()
        signUpButton.checkIsDisplayed()
    }
}
