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

package me.proton.core.auth.presentation.ui.signup

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivitySignupBinding
import me.proton.core.auth.presentation.entity.signup.SignUpInput
import me.proton.core.auth.presentation.entity.signup.SignUpResult
import me.proton.core.auth.presentation.ui.AuthActivity
import me.proton.core.auth.presentation.viewmodel.LoginViewModel
import me.proton.core.auth.presentation.viewmodel.signup.SignupViewModel
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.ui.PlansFragment
import me.proton.core.plan.presentation.ui.removePlans
import me.proton.core.plan.presentation.ui.showPlans
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class SignupActivity : AuthActivity<ActivitySignupBinding>(ActivitySignupBinding::inflate) {

    private val signUpViewModel by viewModels<SignupViewModel>()
    private val loginViewModel by viewModels<LoginViewModel>()

    private val input: SignUpInput by lazy {
        val value = requireNotNull(intent?.extras?.getParcelable(ARG_INPUT)) as SignUpInput
        signUpViewModel.currentAccountType = value.requiredAccountType
        value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpViewModel.register(this)
        signUpViewModel.observeHumanVerification(this)
        supportFragmentManager.showUsernameChooser(requiredAccountType = input.requiredAccountType)

        signUpViewModel.inputState.onEach {
            when (it) {
                is SignupViewModel.InputState.Ready -> {
                    supportFragmentManager.showPlans(planInput = PlanInput())
                    supportFragmentManager.setFragmentResultListener(
                        PlansFragment.KEY_PLAN_SELECTED, this
                    ) { _, bundle ->
                        supportFragmentManager.removePlans()
                        val plan = bundle.getParcelable<SelectedPlan>(PlansFragment.BUNDLE_KEY_PLAN)
                        val billing = bundle.getParcelable<BillingResult>(PlansFragment.BUNDLE_KEY_BILLING_DETAILS)
                        if (plan != null) {
                            onPlanSelected(plan, billing)
                        } else {
                            signUpViewModel.onPlanChooserCancel()
                        }
                    }
                }
            }.exhaustive
        }.launchIn(lifecycleScope)

        signUpViewModel.userCreationState.onEach {
            when (it) {
                is SignupViewModel.State.Idle -> Unit
                is SignupViewModel.State.HumanVerificationNeeded,
                is SignupViewModel.State.Processing -> showLoading(true)
                is SignupViewModel.State.Error.HumanVerification -> Unit
                is SignupViewModel.State.Error.Message -> showError(it.message)
                is SignupViewModel.State.Error.PlanChooserCancel -> Unit
                is SignupViewModel.State.Success -> onSignUpSuccess(it.loginUsername, it.password)
            }.exhaustive
        }.launchIn(lifecycleScope)

        loginViewModel.state.onEach {
            when (it) {
                is LoginViewModel.State.Idle -> showLoading(false)
                is LoginViewModel.State.Processing -> showLoading(true)
                is LoginViewModel.State.Success.UserUnLocked -> onLoginSuccess(it.userId)
                is LoginViewModel.State.Error.CannotUnlockPrimaryKey -> onUnlockUserError(it.error)
                is LoginViewModel.State.Error.UserCheckError -> onLoginError(it.error.localizedMessage)
                is LoginViewModel.State.Error.Message -> onLoginError(it.message)
                is LoginViewModel.State.Need.ChangePassword,
                is LoginViewModel.State.Need.ChooseUsername,
                is LoginViewModel.State.Need.SecondFactor,
                is LoginViewModel.State.Need.TwoPassMode -> {
                    // we are not interested in these events
                }
            }.exhaustive
        }.launchIn(lifecycleScope)
    }

    private fun onPlanSelected(plan: SelectedPlan, billingResult: BillingResult?) {
        if (billingResult == null) {
            signUpViewModel.startCreateUserWorkflow()
        } else {
            val cycle = when (plan.cycle) {
                PlanCycle.MONTHLY -> SubscriptionCycle.MONTHLY
                PlanCycle.YEARLY -> SubscriptionCycle.YEARLY
                PlanCycle.TWO_YEARS -> SubscriptionCycle.TWO_YEARS
            }.exhaustive
            signUpViewModel.startCreatePaidUserWorkflow(plan.planName, plan.planDisplayName, cycle, billingResult)
        }
    }

    private fun onSignUpSuccess(loginUsername: String, encryptedPassword: EncryptedString) {
        with(supportFragmentManager) {
            for (i in 0..backStackEntryCount) {
                popBackStackImmediate()
            }
        }
        binding.lottieProgress.visibility = View.VISIBLE

        loginViewModel.startLoginWorkflowWithEncryptedPassword(
            loginUsername,
            encryptedPassword,
            signUpViewModel.currentAccountType,
            signUpViewModel.subscriptionDetails
        )
    }

    private fun onLoginError(message: String? = null) {
        MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle(R.string.presentation_alert_title)
            .setMessage(message ?: getString(R.string.auth_login_general_error))
            .setPositiveButton(R.string.presentation_alert_ok) { _: DialogInterface, _: Int -> finish() }
            .show()
    }

    private fun onLoginSuccess(userId: UserId) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    ARG_RESULT,
                    with(signUpViewModel) {
                        SignUpResult(
                            accountType = currentAccountType,
                            username = username,
                            domain = domain,
                            email = externalEmail,
                            userId = userId.id
                        )
                    }
                )
            }
        )
        finish()
    }

    companion object {
        const val ARG_INPUT = "arg.signUpInput"
        const val ARG_RESULT = "arg.signUpResult"
    }
}
