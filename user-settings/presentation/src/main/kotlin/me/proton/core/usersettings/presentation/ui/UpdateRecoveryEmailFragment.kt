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

package me.proton.core.usersettings.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.presentation.alert.showPasswordEnterDialog
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.successSnack
import me.proton.core.presentation.utils.validateEmail
import me.proton.core.usersettings.presentation.R
import me.proton.core.usersettings.presentation.databinding.FragmentUpdateRecoveryEmailBinding
import me.proton.core.usersettings.presentation.entity.SettingsInput
import me.proton.core.usersettings.presentation.viewmodel.UpdateRecoveryEmailViewModel
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class UpdateRecoveryEmailFragment : ProtonFragment<FragmentUpdateRecoveryEmailBinding>() {

    private val viewModel by viewModels<UpdateRecoveryEmailViewModel>()

    private val input: SettingsInput by lazy {
        requireArguments().get(ARG_INPUT) as SettingsInput
    }

    private val userId: UserId by lazy { input.user }

    override fun layoutId() = R.layout.fragment_update_recovery_email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addOnBackPressedCallback { setFragmentResult() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? UpdateRecoveryEmailActivity)?.binding?.toolbar?.apply {
            setNavigationOnClickListener { setFragmentResult() }
        }
        binding.saveButton.onClick {
            onSaveClicked()
        }
        viewModel.state.onEach {
            when (it) {
                is UpdateRecoveryEmailViewModel.State.Error.Message -> showError(it.message)
                is UpdateRecoveryEmailViewModel.State.Idle -> Unit
                is UpdateRecoveryEmailViewModel.State.LoadingCurrent -> showLoading(true)
                is UpdateRecoveryEmailViewModel.State.UpdatingCurrent -> showLoading(true)
                is UpdateRecoveryEmailViewModel.State.LoadingSuccess -> {
                    showLoading(false)
                    setCurrentRecoveryEmail(it.recoveryEmail)
                }
                is UpdateRecoveryEmailViewModel.State.UpdatingSuccess -> {
                    binding.newEmailInput.text = ""
                    binding.confirmNewEmailInput.text = ""
                    findOutCurrentRecoveryAddress()
                    binding.root.successSnack(R.string.settings_recovery_email_success)
                }
            }.exhaustive
        }.launchIn(lifecycleScope)

        findOutCurrentRecoveryAddress()
    }

    private fun onSaveClicked() {
        hideKeyboard()
        binding.newEmailInput.apply {
            validateEmail()
                .onFailure { setInputError(getString(R.string.settings_validation_email)) }
                .onSuccess { email -> validateConfirmRecoveryEmailField(email) }
        }
    }

    private fun validateConfirmRecoveryEmailField(
        newRecoveryEmail: String
    ) = with(binding) {
        val confirmedRecoveryEmail = confirmNewEmailInput.text.toString()
        if (newRecoveryEmail == confirmedRecoveryEmail) {
            childFragmentManager.showPasswordEnterDialog(
                secondFactor = viewModel.secondFactorEnabled!!
            ) { password: String, secondFactorCode: String ->
                viewModel.updateRecoveryEmail(
                    userId = input.user,
                    newRecoveryEmail = confirmNewEmailInput.text.toString(),
                    password = password,
                    secondFactorCode = secondFactorCode
                )
            }
        } else {
            confirmNewEmailInput.setInputError(getString(R.string.settings_recovery_email_error_no_match))
        }
    }

    private fun findOutCurrentRecoveryAddress() {
        viewModel.getCurrentRecoveryAddress(userId)
    }

    private fun setCurrentRecoveryEmail(recoveryEmail: String?) {
        binding.saveButton.isEnabled = true
        recoveryEmail?.let {
            if (it.isNotEmpty()) {
                binding.currentEmailInput.text = it
            } else {
                binding.currentEmailInput.hintText = getString(R.string.settings_not_set)
            }
        } ?: run { binding.currentEmailInput.hintText = getString(R.string.settings_not_set) }
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            saveButton.setLoading()
        } else {
            saveButton.setIdle()
        }
    }

    private fun showError(message: String?) {
        showLoading(false)
        binding.root.errorSnack(
            message = message ?: getString(R.string.settings_general_error)
        )
    }

    private fun setFragmentResult() {
        parentFragmentManager.setFragmentResult(
            KEY_UPDATE_RESULT, bundleOf(ARG_UPDATE_RESULT to null)
        )
    }

    companion object {
        const val KEY_UPDATE_RESULT = "key.update_result"
        const val ARG_UPDATE_RESULT = "arg.update_result"
        const val ARG_INPUT = "arg.settingsInput"

        operator fun invoke(input: SettingsInput) = UpdateRecoveryEmailFragment().apply {
            arguments = bundleOf(
                ARG_INPUT to input
            )
        }
    }
}
