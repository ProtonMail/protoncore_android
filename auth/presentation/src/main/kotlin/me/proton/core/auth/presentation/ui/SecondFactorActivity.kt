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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.Activity2faBinding
import me.proton.core.auth.presentation.entity.NextStep
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.SecondFactorResult
import me.proton.core.auth.presentation.viewmodel.SecondFactorViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validate
import me.proton.core.util.kotlin.exhaustive

/**
 * Second Factor Activity responsible for entering the second factor code.
 * It also supports recovery code mode, which allows the user to enter a second factor recovery code.
 * Optional, only shown for accounts with 2FA login enabled.
 */
@AndroidEntryPoint
class SecondFactorActivity : AuthActivity<Activity2faBinding>() {

    private val input: SecondFactorInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    // initial mode is the second factor input mode.
    private var mode = Mode.TWO_FACTOR

    private val viewModel by viewModels<SecondFactorViewModel>()

    override fun layoutId(): Int = R.layout.activity_2fa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            closeButton.onClick {
                onBackPressed()
            }

            recoveryCodeButton.onClick {
                when (mode) {
                    Mode.TWO_FACTOR -> switchToRecovery()
                    Mode.RECOVERY_CODE -> switchToTwoFactor()
                }
            }

            authenticateButton.onClick(::onAuthenticateClicked)
            secondFactorInput.setOnFocusLostListener { _, _ ->
                secondFactorInput.validate()
                    .onFailure { secondFactorInput.setInputError() }
                    .onSuccess { secondFactorInput.clearInputError() }
            }
        }

        viewModel.state.onEach {
            when (it) {
                is SecondFactorViewModel.State.Idle -> showLoading(false)
                is SecondFactorViewModel.State.Processing -> showLoading(true)
                is SecondFactorViewModel.State.Success.UserUnLocked -> onSuccess(it.userId, NextStep.None)
                is SecondFactorViewModel.State.Need.TwoPassMode -> onSuccess(it.userId, NextStep.TwoPassMode)
                is SecondFactorViewModel.State.Need.ChooseUsername -> onSuccess(it.userId, NextStep.ChooseAddress)
                is SecondFactorViewModel.State.Need.ChangePassword -> onSuccess(it.userId, NextStep.None)
                is SecondFactorViewModel.State.Error.CannotUnlockPrimaryKey -> onUnlockUserError(it.error)
                is SecondFactorViewModel.State.Error.Message -> onError(false, it.message)
                is SecondFactorViewModel.State.Error.Unrecoverable -> {
                    showError(getString(R.string.auth_login_general_error))
                    onBackPressed()
                }
            }.exhaustive
        }.launchIn(lifecycleScope)
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            authenticateButton.setLoading()
        } else {
            authenticateButton.setIdle()
        }
        secondFactorInput.isEnabled = !loading
    }

    private fun onAuthenticateClicked() {
        hideKeyboard()
        with(binding) {
            secondFactorInput.validate()
                .onFailure { secondFactorInput.setInputError() }
                .onSuccess { secondFactorCode ->
                    viewModel.startSecondFactorFlow(
                        userId = UserId(input.userId),
                        password = input.password,
                        requiredAccountType = input.requiredAccountType,
                        isTwoPassModeNeeded = input.isTwoPassModeNeeded,
                        secondFactorCode = secondFactorCode
                    )
                }
        }
    }

    override fun onBackPressed() {
        viewModel.stopSecondFactorFlow(UserId(input.userId))
            .invokeOnCompletion { finish() }
    }

    private fun onSuccess(
        userId: UserId,
        nextStep: NextStep
    ) {
        val intent = Intent()
            .putExtra(ARG_RESULT, SecondFactorResult(userId = userId.id, nextStep = nextStep))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onError(triggerValidation: Boolean, message: String?) {
        if (triggerValidation) {
            binding.secondFactorInput.setInputError()
        }
        showError(message)
    }

    /**
     * Switches the mode to recovery code. It also handles the UI for the new mode.
     */
    private fun Activity2faBinding.switchToRecovery() {
        mode = Mode.RECOVERY_CODE
        secondFactorInput.apply {
            text = ""
            helpText = getString(R.string.auth_2fa_recovery_code_assistive_text)
            labelText = getString(R.string.auth_2fa_recovery_code_label)
            inputType = InputType.TYPE_CLASS_TEXT
        }
        recoveryCodeButton.text = getString(R.string.auth_2fa_use_2fa_code)
    }

    /**
     * Switches the mode to second factor code. It also handles the UI for the new mode.
     */
    private fun Activity2faBinding.switchToTwoFactor() {
        mode = Mode.TWO_FACTOR
        secondFactorInput.apply {
            text = ""
            helpText = getString(R.string.auth_2fa_assistive_text)
            labelText = getString(R.string.auth_2fa_label)
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        recoveryCodeButton.text = getString(R.string.auth_2fa_use_recovery_code)
    }

    /**
     * Working modes of this View.
     */
    private enum class Mode {
        TWO_FACTOR,
        RECOVERY_CODE
    }

    companion object {
        const val ARG_INPUT = "arg.secondFactorInput"
        const val ARG_RESULT = "arg.secondFactorResult"
    }
}
