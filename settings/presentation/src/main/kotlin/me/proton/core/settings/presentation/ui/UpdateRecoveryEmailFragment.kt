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

package me.proton.core.settings.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.onClick
import me.proton.core.settings.presentation.R
import me.proton.core.settings.presentation.databinding.FragmentUpdateRecoveryEmailBinding
import me.proton.core.settings.presentation.entity.Settings
import me.proton.core.settings.presentation.entity.SettingsInput
import me.proton.core.settings.presentation.viewmodel.UpdateRecoveryEmailViewModel
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class UpdateRecoveryEmailFragment : ProtonFragment<FragmentUpdateRecoveryEmailBinding>() {

    private val viewModel by viewModels<UpdateRecoveryEmailViewModel>()

    private val input: SettingsInput by lazy {
        requireArguments().get(ARG_INPUT) as SettingsInput
    }

    private val userId: UserId by lazy { input.user }

    private val settings: Settings? by lazy { input.settings }

    override fun layoutId() = R.layout.fragment_update_recovery_email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finish()
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as UpdateRecoveryEmailActivity).binding.toolbar.apply {
            setNavigationOnClickListener {
                finish()
            }
        }
        binding.apply {
            saveButton.onClick {
                childFragmentManager.showPasswordEnterDialog(context = requireContext()) { password: String, twoFA: String ->
                    viewModel.updateRecoveryEmail(
                        userId = input.user,
                        newRecoveryEmail = confirmNewEmailInput.text.toString(),
                        username = input.username,
                        password = password,
                        twoFactorCode = twoFA)
                }
            }
        }
        viewModel.currentRecoveryEmailState.onEach {
            when (it) {
                is UpdateRecoveryEmailViewModel.CurrentRecoveryEmailState.Error.Message -> {

                }
                is UpdateRecoveryEmailViewModel.CurrentRecoveryEmailState.Idle,
                is UpdateRecoveryEmailViewModel.CurrentRecoveryEmailState.Processing -> {

                }
                is UpdateRecoveryEmailViewModel.CurrentRecoveryEmailState.Success -> {
                    setCurrentRecoveryEmail(it.recoveryEmail)
                }
            }.exhaustive
        }.launchIn(lifecycleScope)
        settings?.let {
            setCurrentRecoveryEmail(it.email?.value)
        } ?: run {
            findOutCurrentRecoveryAddress()
        }
    }

    private fun findOutCurrentRecoveryAddress() {
        viewModel.getCurrentRecoveryAddress(userId)
    }

    private fun setCurrentRecoveryEmail(recoveryEmail: String?) {
        recoveryEmail?.let {
            if (it.isNotEmpty()) {
                binding.currentEmailInput.text = it
            } else {
                binding.currentEmailInput.hintText = getString(R.string.settings_not_set)
            }
        } ?: run { binding.currentEmailInput.hintText = getString(R.string.settings_not_set) }
    }

    private fun finish() {
        parentFragmentManager.setFragmentResult(
            KEY_UPDATE_RESULT, bundleOf(BUNDLE_KEY_PLAN to null)
        )
        parentFragmentManager.popBackStackImmediate()
    }

    companion object {
        const val KEY_UPDATE_RESULT = "key.update_result"
        const val BUNDLE_KEY_PLAN = "bundle.update_result"
        const val ARG_INPUT = "arg.settingsInput"

        operator fun invoke(input: SettingsInput) = UpdateRecoveryEmailFragment().apply {
            arguments = bundleOf(
                ARG_INPUT to input
            )
        }
    }
}
