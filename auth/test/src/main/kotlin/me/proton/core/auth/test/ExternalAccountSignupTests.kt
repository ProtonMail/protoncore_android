/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.auth.test

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.entity.signup.SignUpInput
import me.proton.core.auth.presentation.ui.StartSignup
import me.proton.core.auth.presentation.ui.signup.SignupActivity
import me.proton.core.humanverification.presentation.HumanVerificationInitializer
import me.proton.core.test.android.robots.auth.ChooseUsernameRobot
import me.proton.core.test.android.robots.auth.signup.PasswordSetupRobot
import me.proton.core.test.android.robots.auth.signup.SignupFinishedRobot
import me.proton.core.test.android.robots.humanverification.HVCodeRobot
import me.proton.core.test.quark.Quark
import kotlin.test.BeforeTest
import kotlin.test.Test
import me.proton.core.test.quark.data.User as TestUser

/** Tests for signing up with an external account.
 * Only for apps that provide [AccountType.External].
 */
public abstract class ExternalAccountSignupTests {
    protected abstract val quark: Quark

    private lateinit var testUser: TestUser

    @BeforeTest
    internal fun setUp() {
        testUser = TestUser(
            name = "",
            email = "${TestUser.randomUsername()}@externaldomain.test",
            isExternal = true
        )
        quark.jailUnban()
        HumanVerificationInitializer().create(ApplicationProvider.getApplicationContext())
    }

    @Test
    internal fun happyPath() = withSignupActivity(AccountType.External) {
        ChooseUsernameRobot()
            .apply { verify { domainInputNotDisplayed() } }
            .username(testUser.email)
            .next()

        HVCodeRobot()
            .setCode("666666")
            .verifyCode(PasswordSetupRobot::class.java)
            .apply { verify { passwordSetupElementsDisplayed() } }
            .setAndConfirmPassword<HVCodeRobot>(testUser.password)
            .verifyCode(SignupFinishedRobot::class.java)
            .verify { signupFinishedDisplayed() }
    }

    @Test
    internal fun incorrectEmailVerificationCode() = withSignupActivity(AccountType.External) {
        ChooseUsernameRobot()
            .apply { verify { domainInputNotDisplayed() } }
            .username(testUser.email)
            .next()

        HVCodeRobot()
            .setCode("000111")
            .verifyCode(HVCodeRobot::class.java)
            .verify { incorrectCode() }
    }

    @Test
    internal fun externalSignupNotSupported() = withSignupActivity(AccountType.Internal) {
        ChooseUsernameRobot()
            .apply {
                verify {
                    domainInputDisplayed()
                    accountTypeSwitchNotDisplayed()
                }
            }
            .username(testUser.email)
            .next()
            .verify {
                errorSnackbarDisplayed("Username contains invalid characters")
            }
    }

    protected companion object {
        protected fun launchSignupActivity(accountType: AccountType): ActivityScenario<SignupActivity> =
            ActivityScenario.launch(
                StartSignup().createIntent(
                    ApplicationProvider.getApplicationContext(),
                    SignUpInput(accountType)
                )
            )

        protected inline fun withSignupActivity(
            accountType: AccountType,
            body: (ActivityScenario<SignupActivity>) -> Unit
        ) {
            launchSignupActivity(accountType).use(body)
        }
    }
}
