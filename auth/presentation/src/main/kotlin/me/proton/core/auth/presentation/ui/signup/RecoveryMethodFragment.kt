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

package me.proton.core.auth.presentation.ui.signup

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentSignupRecoveryBinding
import me.proton.core.auth.presentation.entity.signup.RecoveryMethodType
import me.proton.core.auth.presentation.viewmodel.signup.RecoveryMethodViewModel
import me.proton.core.auth.presentation.viewmodel.signup.SignupViewModel
import me.proton.core.observability.domain.metrics.SignupScreenViewTotalV1
import me.proton.core.presentation.ui.alert.FragmentDialogResultLauncher
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.telemetry.domain.entity.TelemetryPriority
import me.proton.core.telemetry.presentation.annotation.MenuItemClicked
import me.proton.core.telemetry.presentation.annotation.ProductMetrics
import me.proton.core.telemetry.presentation.annotation.ScreenClosed
import me.proton.core.telemetry.presentation.annotation.ScreenDisplayed
import me.proton.core.telemetry.presentation.annotation.ViewClicked
import me.proton.core.util.kotlin.exhaustive

@ProductMetrics(
    group = "account.any.signup",
    flow = "mobile_signup_full"
)
@ScreenDisplayed(
    event = "fe.recovery_method.displayed",
    priority = TelemetryPriority.Immediate
)
@ScreenClosed(
    event = "user.recovery_method.closed",
    priority = TelemetryPriority.Immediate
)
@ViewClicked(
    event = "user.recovery_method.clicked",
    viewIds = [
        "email",
        "phone",
        "next",
        "terms"
    ],
    priority = TelemetryPriority.Immediate
)
@MenuItemClicked(
    event = "user.recovery_method.clicked",
    toolbarId = "toolbar",
    itemIds = ["skip"],
    priority = TelemetryPriority.Immediate
)
@AndroidEntryPoint
class RecoveryMethodFragment : SignupFragment(R.layout.fragment_signup_recovery) {
    private val canSkipRecoveryMethod get() = signupViewModel.currentAccountType != AccountType.Username

    private val viewModel by viewModels<RecoveryMethodViewModel>()
    private val signupViewModel by activityViewModels<SignupViewModel>()
    private val binding by viewBinding(FragmentSignupRecoveryBinding::bind)

    private lateinit var skipRecoveryDialogResultLauncher: FragmentDialogResultLauncher<Unit>

    override fun onBackPressed() {
        parentFragmentManager.popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skipRecoveryDialogResultLauncher = childFragmentManager.registerSkipRecoveryDialogResultLauncher(this) {
            viewModel.onRecoveryMethodDestinationSkipped()
            signupViewModel.skipRecoveryMethod()
        }


        binding.apply {
            toolbar.apply {
                setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.skip -> {
                            showSkip()
                            true
                        }
                        else -> false
                    }
                }
            }
            initTabs()
            next.onClick(::onNextClicked)

            adjustAccountTypeUI()
            adjustSkippingRecoveryUI()
        }

        viewModel.recoveryMethodUpdate
            .onEach { setActiveVerificationMethod(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.validationResult.onEach {
            when (it) {
                is RecoveryMethodViewModel.ValidationState.None -> Unit
                is RecoveryMethodViewModel.ValidationState.Error -> showError(it.throwable?.getUserMessage(resources))
                is RecoveryMethodViewModel.ValidationState.Processing -> showLoading(true)
                is RecoveryMethodViewModel.ValidationState.Skipped -> {}
                is RecoveryMethodViewModel.ValidationState.Success -> {
                    if (it.value) {
                        // if recovery destination is valid
                        signupViewModel.setRecoveryMethod(viewModel.recoveryMethod)
                    } else {
                        showError(getString(R.string.auth_signup_error_validation_recovery_destination))
                    }
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        observeProcessingStates()

        launchOnScreenView {
            signupViewModel.onScreenView(SignupScreenViewTotalV1.ScreenId.setRecoveryMethod)
        }
    }

    /** Adjusts the UI, depending on the current account type. */
    private fun adjustAccountTypeUI() = with(binding) {
        recoveryOptions.isVisible = signupViewModel.currentAccountType != AccountType.Username

        recoveryInstructions.setText(
            if (signupViewModel.currentAccountType == AccountType.Username) {
                R.string.auth_signup_recovery_method_subtitle_email_only
            } else {
                R.string.auth_signup_recovery_method_subtitle
            }
        )
    }

    /** Adjust the UI, depending on [canSkipRecoveryMethod] flag. */
    private fun adjustSkippingRecoveryUI() = with(binding) {
        titleText.setText(
            if (canSkipRecoveryMethod) {
                R.string.auth_signup_recovery_method_title
            } else {
                R.string.auth_signup_recovery_method_title_mandatory
            }
        )

        toolbar.menu.findItem(R.id.skip)?.isVisible = canSkipRecoveryMethod
    }

    private fun initTabs() = with(binding) {
        recoveryOptions.apply {
            addTab(
                newTab().apply {
                    id = R.id.email
                    text = getString(R.string.auth_signup_recovery_method_email)
                    tag = RecoveryMethodType.EMAIL
                }
            )
            addTab(
                newTab().apply {
                    id = R.id.phone
                    text = getString(R.string.auth_signup_recovery_method_phone)
                    tag = RecoveryMethodType.SMS
                }
            )

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        val recoveryMethod = tab.tag as RecoveryMethodType
                        viewModel.setActiveRecoveryMethod(
                            userSelectedMethodType = recoveryMethod
                        )
                    }
                }
            })
        }
    }

    private fun observeProcessingStates() {
        signupViewModel.state.onEach {
            when (it) {
                is SignupViewModel.State.Idle,
                is SignupViewModel.State.CreateUserInputReady -> Unit
                is SignupViewModel.State.CreateUserSuccess,
                is SignupViewModel.State.PreloadingPlans,
                is SignupViewModel.State.CreateUserProcessing -> showLoading(true)
                is SignupViewModel.State.Error.CreateUserCanceled,
                is SignupViewModel.State.Error.PlanChooserCanceled,
                is SignupViewModel.State.Error.Message -> showLoading(false)
            }.exhaustive
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setActiveVerificationMethod(methodType: RecoveryMethodType) {
        val containerId = binding.fragmentOptionsContainer.id
        when (methodType) {
            RecoveryMethodType.EMAIL -> childFragmentManager.showEmailRecoveryMethodFragment(containerId)
            RecoveryMethodType.SMS -> childFragmentManager.showSMSRecoveryMethodFragment(containerId)
        }.exhaustive
    }

    private fun onNextClicked() {
        hideKeyboard()
        when {
            viewModel.recoveryMethod.isSet -> viewModel.validateRecoveryDestinationInput()
            canSkipRecoveryMethod -> showSkip()
            else -> viewModel.onRecoveryMethodDestinationMissing()
        }
    }

    private fun showSkip() {
        hideKeyboard()
        skipRecoveryDialogResultLauncher.show(Unit)
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            next.setLoading()
        } else {
            next.setIdle()
        }
    }

    companion object {
        operator fun invoke() = RecoveryMethodFragment()
    }
}
