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
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
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
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.presentation.entity.Cycle
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.ui.PlansFragment
import me.proton.core.plan.presentation.ui.removePlans
import me.proton.core.plan.presentation.ui.showPlans
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class SignupActivity : AuthActivity<ActivitySignupBinding>() {

    private val signUpViewModel by viewModels<SignupViewModel>()
    private val loginViewModel by viewModels<LoginViewModel>()

    private val input: SignUpInput by lazy {
        val value = requireNotNull(intent?.extras?.getParcelable(ARG_INPUT)) as SignUpInput
        signUpViewModel.currentAccountType = value.requiredAccountType
        value
    }

    override fun layoutId() = R.layout.activity_signup

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
                is SignupViewModel.State.Processing -> showLoading(true)
                is SignupViewModel.State.Error.HumanVerification -> Unit
                is SignupViewModel.State.Error.Message -> showError(it.message)
                is SignupViewModel.State.Error.PlanChooserCancel -> Unit
                is SignupViewModel.State.Success -> onSignUpSuccess()
            }.exhaustive
        }.launchIn(lifecycleScope)

        loginViewModel.state.onEach {
            when (it) {
                is LoginViewModel.State.Idle -> showLoading(false)
                is LoginViewModel.State.Processing -> showLoading(true)
                is LoginViewModel.State.Success.UserUnLocked -> onLoginSuccess(it.userId)
                is LoginViewModel.State.Error.CannotUnlockPrimaryKey -> onUnlockUserError(it.error)
                is LoginViewModel.State.Error.UserCheckError -> onError(true, it.error.localizedMessage)
                is LoginViewModel.State.Error.Message -> onError(true, it.message)
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
                Cycle.MONTHLY -> SubscriptionCycle.MONTHLY
                Cycle.YEARLY -> SubscriptionCycle.YEARLY
            }.exhaustive
            signUpViewModel.startCreatePaidUserWorkflow(plan.planId, plan.planName, cycle, billingResult)
        }
    }

    private fun onSignUpSuccess() {
        with(supportFragmentManager) {
            for (i in 0..backStackEntryCount) {
                popBackStackImmediate()
            }
        }
        binding.lottieProgress.visibility = View.VISIBLE
        loginViewModel.startLoginWorkflow(
            signUpViewModel.getLoginUsername()!!,
            signUpViewModel.password,
            signUpViewModel.currentAccountType,
            signUpViewModel.subscriptionDetails
        )
    }

    override fun onError(triggerValidation: Boolean, message: String?) {
        showError(message)
        finish()
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
