package me.proton.core.auth.test.robot

import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.auth.presentation.R
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.test.fusion.Fusion.view
import org.hamcrest.CoreMatchers

/** Corresponds to [me.proton.core.auth.presentation.ui.ChooseAddressActivity]. */
public object ChooseInternalAddressRobot {
    private val domainInput = view.withId(R.id.domainInput)
    private val usernameInput = view.withCustomMatcher(inputFieldMatcher(R.id.usernameInput))
    private val continueButton = view.withId(R.id.nextButton)
    private val cancelButton = view.withId(R.id.cancelButton)

    public fun fillUsername(username: String): ChooseInternalAddressRobot = apply {
        usernameInput.typeText(username)
    }

    public fun cancel() {
        cancelButton.click()
    }

    public fun next() {
        continueButton.click()
    }

    public fun selectAlternativeDomain(): ChooseInternalAddressRobot = apply {
        domainInput.click()

        view
            .containsText("protonmail")
            .withRootMatcher(isPlatformPopup())
            .click()
    }

    public fun selectPrimaryDomain(): ChooseInternalAddressRobot = apply {
        domainInput.click()

        view
            .withCustomMatcher(ViewMatchers.withText(CoreMatchers.not(CoreMatchers.containsString("protonmail"))))
            .withRootMatcher(isPlatformPopup())
            .click()
    }

    public fun continueButtonIsEnabled() {
        continueButton.await {
            checkIsDisplayed()
            checkIsEnabled()
        }
    }

    public fun domainInputDisplayed() {
        domainInput.await {
            checkIsDisplayed()
        }
    }

    public fun usernameInputIsEmpty() {
        usernameInput.await {
            checkLengthEquals(0)
        }
    }

    public fun usernameInputIsFilled(with: String) {
        usernameInput.await {
            checkContainsText(with)
        }
    }
}
