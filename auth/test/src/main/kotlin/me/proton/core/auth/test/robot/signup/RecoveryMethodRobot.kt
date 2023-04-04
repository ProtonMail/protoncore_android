package me.proton.core.auth.test.robot.signup

import me.proton.core.auth.presentation.R
import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import me.proton.test.fusion.Fusion.view

public object RecoveryMethodRobot {
    private val skipMenuButton = view.withId(R.id.recovery_menu_skip)

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
