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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.test.flow.SignInFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.login.LoginRobot
import kotlin.test.Test

/**
 * Minimal SignIn Tests for app providing [AccountType.External].
 */
public interface MinimalSignInExternalTests {

    public val context: Context
        get() = ApplicationProvider.getApplicationContext()

    public val isSsoEnabled: Boolean
        get() = context.resources.getBoolean(R.bool.core_feature_auth_sso_enabled)

    public fun verifyAfter()

    @Test
    public fun signInWithSsoHappyPath() {
        if (isSsoEnabled) {
            AddAccountRobot.clickSignIn()
            LoginRobot.signInWithSSO()
            SignInFlow.signInSso()

            verifyAfter()
        }
    }
}
