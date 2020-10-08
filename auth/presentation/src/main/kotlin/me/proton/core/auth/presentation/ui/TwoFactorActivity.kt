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

package me.proton.core.auth.presentation.ui

import android.os.Bundle
import android.text.InputType
import me.proton.android.core.presentation.utils.onClick
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.Activity2faBinding

/**
 * Two-Factor Activity responsible for entering the 2FA code.
 * Optional, only shown for accounts with 2FA login enabled.
 * @author Dino Kadrikj.
 */
class TwoFactorActivity : ProtonAuthActivity<Activity2faBinding>() {
    override fun layoutId(): Int = R.layout.activity_2fa

    private var mode = Mode.TWO_FACTOR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            closeButton.onClick {
                finish()
            }

            recoveryCodeButton.onClick {
                when (mode) {
                    Mode.TWO_FACTOR -> switchToRecovery()
                    Mode.RECOVERY_CODE -> switchToTwoFactor()
                }
            }
        }
    }

    /**
     * Switches the mode to recovery code. It also handles the UI for the new mode.
     */
    private fun Activity2faBinding.switchToRecovery() {
        mode = Mode.RECOVERY_CODE
        twoFactorInput.text = ""
        twoFactorInput.helpText = getString(R.string.auth_2fa_recovery_code_assistive_text)
        twoFactorInput.labelText = getString(R.string.auth_2fa_recovery_code_label)
        twoFactorInput.inputType = InputType.TYPE_CLASS_TEXT
        recoveryCodeButton.text = getString(R.string.auth_2fa_use_2fa_code)
    }

    /**
     * Switches the mode to two factor code. It also handles the UI for the new mode.
     */
    private fun Activity2faBinding.switchToTwoFactor() {
        mode = Mode.TWO_FACTOR
        twoFactorInput.text = ""
        twoFactorInput.helpText = getString(R.string.auth_2fa_assistive_text)
        twoFactorInput.labelText = getString(R.string.auth_2fa_label)
        twoFactorInput.inputType = InputType.TYPE_CLASS_NUMBER
        recoveryCodeButton.text = getString(R.string.auth_2fa_use_recovery_code)
    }

    private enum class Mode {
        TWO_FACTOR,
        RECOVERY_CODE
    }
}
