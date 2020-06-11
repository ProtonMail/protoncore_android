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

package me.proton.core.humanverification.presentation.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import me.proton.android.core.presentation.ui.ProtonDialogFragment
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.DialogHumanVerificationMainBinding
import me.proton.core.humanverification.presentation.utils.showHumanVerificationCaptchaContent
import me.proton.core.humanverification.presentation.utils.showHumanVerificationEmailContent
import me.proton.core.humanverification.presentation.utils.showHumanVerificationSMSContent
import me.proton.core.humanverification.presentation.viewmodel.HumanVerificationViewModel
import me.proton.core.humanverification.presentation.viewmodel.HumanVerificationViewModelFactory

/**
 * Created by dinokadrikj on 5/14/20.
 *
 * Shows the dialog for the Human Verification options and option procedures.
 */
class HumanVerificationDialog :
    ProtonDialogFragment<HumanVerificationViewModel, DialogHumanVerificationMainBinding>() {

    companion object {
        private const val ARG_VERIFICATION_OPTIONS = "arg.verification-options"

        operator fun invoke(availableVerificationMethods: List<String>): HumanVerificationDialog =
            HumanVerificationDialog().apply {
                val args = bundleOf(ARG_VERIFICATION_OPTIONS to availableVerificationMethods)
                if (arguments != null) requireArguments().putAll(args)
                else arguments = args
            }
    }

    private val humanVerificationViewModel by viewModels<HumanVerificationViewModel> {
        HumanVerificationViewModelFactory(requireArguments().get(ARG_VERIFICATION_OPTIONS) as List<String>)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.enabledMethods.observe(this) {
            doOnData { setEnabledVerificationMethods(it) }
        }
        viewModel.activeMethod.observe(this) {
            doOnData { setActiveVerificationMethod(it) }
        }
        binding.headerOptions.verificationOptions.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) viewModel.defineActiveVerificationMethod(
                when (checkedId) {
                    binding.headerOptions.verificationCaptcha.id -> "captcha"
                    binding.headerOptions.verificationEmail.id -> "email"
                    binding.headerOptions.verificationSMS.id -> "sms"
                    else -> throw RuntimeException("Unknown verification option selected!")
                }
            )
        }
    }

    override fun initViewModel() {
        // TODO: dino fix this with DI?
        viewModel = humanVerificationViewModel
    }

    override fun layoutId(): Int = R.layout.dialog_human_verification_main

    private fun setEnabledVerificationMethods(enabledMethods: List<String>) {
        binding.headerOptions.apply {
            verificationCaptcha.visibility =
                if (enabledMethods.contains("captcha")) View.VISIBLE else View.GONE
            verificationEmail.visibility =
                if (enabledMethods.contains("email")) View.VISIBLE else View.GONE
            verificationSMS.visibility =
                if (enabledMethods.contains("sms")) View.VISIBLE else View.GONE
        }
    }

    private fun setActiveVerificationMethod(verificationMethod: String) {
        when (verificationMethod) {
            "captcha" -> {
                binding.headerOptions.apply {
                    verificationOptions.check(verificationCaptcha.id)
                }
                childFragmentManager.showHumanVerificationCaptchaContent(containerId = binding.fragmentOptionsContainer.id)
            }
            "email" -> {
                binding.headerOptions.apply {
                    verificationOptions.check(verificationEmail.id)
                }
                childFragmentManager.showHumanVerificationEmailContent(containerId = binding.fragmentOptionsContainer.id)
            }
            "sms" -> {
                binding.headerOptions.apply {
                    verificationOptions.check(verificationSMS.id)
                }
                childFragmentManager.showHumanVerificationSMSContent(containerId = binding.fragmentOptionsContainer.id)
            }
        }
    }


}
