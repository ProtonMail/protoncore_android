/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.test.android.uitests.suites

import me.proton.core.test.android.uitests.tests.medium.auth.AddAccountTests
import me.proton.core.test.android.uitests.tests.medium.auth.login.AccountSwitcherTests
import me.proton.core.test.android.uitests.tests.medium.auth.login.LoginTests
import me.proton.core.test.android.uitests.tests.medium.auth.login.MailboxTests
import me.proton.core.test.android.uitests.tests.medium.auth.login.TwoFaTests
import me.proton.core.test.android.uitests.tests.medium.auth.signup.ExternalSetupTests
import me.proton.core.test.android.uitests.tests.medium.auth.signup.PasswordSetupTests
import me.proton.core.test.android.uitests.tests.medium.auth.signup.RecoveryMethodsSetupTests
import me.proton.core.test.android.uitests.tests.medium.auth.signup.SelectPlanTests
import me.proton.core.test.android.uitests.tests.medium.auth.signup.UsernameSetupTests
import me.proton.core.test.android.uitests.tests.medium.humanverification.HumanVerificationTests
import me.proton.core.test.android.uitests.tests.medium.payments.ExistingPaymentMethodTests
import me.proton.core.test.android.uitests.tests.medium.payments.NewCreditCardTests
import me.proton.core.test.android.uitests.tests.medium.plans.CurrentPlanTests
import me.proton.core.test.android.uitests.tests.medium.plans.UpgradePlanTests
import me.proton.core.test.android.uitests.tests.medium.usersettings.RecoveryEmailTests
import me.proton.core.test.android.uitests.tests.medium.usersettings.PasswordManagementTests
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    AddAccountTests::class,

    // Login
    LoginTests::class,
    MailboxTests::class,
    TwoFaTests::class,
    HumanVerificationTests::class,
    AccountSwitcherTests::class,

    // Signup
    UsernameSetupTests::class,
    PasswordSetupTests::class,
    RecoveryMethodsSetupTests::class,
    ExternalSetupTests::class,
    SelectPlanTests::class,

    // Payments
    ExistingPaymentMethodTests::class,
    NewCreditCardTests::class,

    // Plans
    CurrentPlanTests::class,
    UpgradePlanTests::class,

    // User Settings
    RecoveryEmailTests::class,
    PasswordManagementTests::class
)
class FullSuite
