/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.auth.test.robot.signup

import me.proton.core.auth.presentation.R
import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import me.proton.test.fusion.Fusion.view

public object RecoveryMethodRobot {
    private val skipMenuButton = view.withId(R.id.skip)

    public fun skip(): SkipRecoveryAlertRobot {
        skipMenuButton.click()
        return SkipRecoveryAlertRobot
    }

    public object SkipRecoveryAlertRobot {
        private val setRecoveryMethodButton =
            view.withText(stringFromResource(R.string.auth_signup_set_recovery))
        private val skipConfirmButton =
            view.withText(stringFromResource(R.string.auth_signup_skip_recovery))

        public fun setRecoveryMethod(): RecoveryMethodRobot {
            setRecoveryMethodButton.click()
            return RecoveryMethodRobot
        }

        public fun skipConfirm() {
            skipConfirmButton.click()
        }
    }
}
