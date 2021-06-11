/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.test.android.uitests

import me.proton.core.test.android.uitests.tests.humanverification.HumanVerificationTests
import me.proton.core.test.android.uitests.tests.login.LoginTests
import me.proton.core.test.android.uitests.tests.login.MailboxTests
import me.proton.core.test.android.uitests.tests.login.TwoFaTests
import me.proton.core.test.android.uitests.tests.payments.ExistingPaymentMethodTests
import me.proton.core.test.android.uitests.tests.payments.NewCreditCardTests
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    LoginTests::class,
    MailboxTests::class,
    TwoFaTests::class,
    HumanVerificationTests::class,
    ExistingPaymentMethodTests::class,
    NewCreditCardTests::class
)
class TestSuite
