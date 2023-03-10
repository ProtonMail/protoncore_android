package me.proton.core.auth.test.robot

import me.proton.core.auth.presentation.R
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.test.fusion.Fusion.view

/** Corresponds to [me.proton.core.auth.presentation.ui.TwoPassModeActivity]. */
public object TwoPassRobot {
    private val passphraseInput =
        view.withCustomMatcher(inputFieldMatcher(R.id.mailboxPasswordInput))
    private val unlockButton = view.withId(R.id.unlockButton)

    public fun fillMailboxPassword(passphrase: String): TwoPassRobot = apply {
        passphraseInput.typeText(passphrase)
    }

    public fun unlock() {
        unlockButton.click()
    }
}
