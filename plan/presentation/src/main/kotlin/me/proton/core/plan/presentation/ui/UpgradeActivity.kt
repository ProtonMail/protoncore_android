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

package me.proton.core.plan.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.onPaymentResult
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.ActivityPlansBinding
import me.proton.core.plan.presentation.entity.Cycle
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.entity.UpgradeResult
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
class UpgradeActivity : ProtonActivity<ActivityPlansBinding>() {

    @Inject
    lateinit var paymentsOrchestrator: PaymentsOrchestrator

    private val input: PlanInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    override fun layoutId() = R.layout.activity_plans

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentsOrchestrator.register(this)

        supportFragmentManager.showPlans(planInput = input)

        supportFragmentManager.setFragmentResultListener(
            PlansFragment.KEY_PLAN_SELECTED, this@UpgradeActivity
        ) { _, bundle ->
            val plan = bundle.getParcelable<SelectedPlan>(PlansFragment.BUNDLE_KEY_PLAN)
            if (plan == null) {
                finish()
            } else {
                openPayments(
                    userId = input.user!!,
                    planId = plan.planId,
                    planName = plan.planName,
                    cycle = when (plan.cycle) {
                        Cycle.MONTHLY -> SubscriptionCycle.MONTHLY
                        Cycle.YEARLY -> SubscriptionCycle.YEARLY
                    }.exhaustive
                )
            }
        }
    }

    private fun openPayments(userId: UserId, planId: String, planName: String, cycle: SubscriptionCycle) {
        with(paymentsOrchestrator) {
            onPaymentResult { result ->
                result?.let {
                    val intent = Intent()
                        .putExtra(ARG_RESULT, UpgradeResult(planId, it))
                    setResult(Activity.RESULT_OK, intent)
                }
                finish()
            }

            startBillingWorkFlow(
                userId = userId,
                selectedPlan = PlanShortDetails(
                    id = planId,
                    name = planName,
                    subscriptionCycle = cycle
                ),
                codes = null
            )
        }
    }

    companion object {
        const val ARG_INPUT = "arg.plansInput"
        const val ARG_RESULT = "arg.plansResult"
    }
}
