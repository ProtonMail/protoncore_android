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
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.ActivityUpgradeBinding
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.entity.UpgradeResult
import me.proton.core.plan.presentation.ui.BasePlansFragment.Companion.BUNDLE_KEY_BILLING_DETAILS
import me.proton.core.plan.presentation.ui.BasePlansFragment.Companion.BUNDLE_KEY_PLAN
import me.proton.core.plan.presentation.ui.BasePlansFragment.Companion.KEY_PLAN_SELECTED
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import javax.inject.Inject

@AndroidEntryPoint
class UpgradeActivity : ProtonViewBindingActivity<ActivityUpgradeBinding>(ActivityUpgradeBinding::inflate) {

    @Inject
    lateinit var paymentsOrchestrator: PaymentsOrchestrator

    private val input: PlanInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentsOrchestrator.register(this)

        if (input.user != null) {
            supportFragmentManager.showPlansForUpgrade(containerId = R.id.layoutContent, planInput = input)
        } else {
            supportFragmentManager.showPlansSignup(containerId = R.id.layoutContent, planInput = input)
        }

        supportFragmentManager.setFragmentResultListener(
            KEY_PLAN_SELECTED, this@UpgradeActivity
        ) { _, bundle ->
            val plan = bundle.getParcelable<SelectedPlan>(BUNDLE_KEY_PLAN)
            val billing = bundle.getParcelable<BillingResult>(BUNDLE_KEY_BILLING_DETAILS)
            if (plan == null || billing == null) {
                finish()
            } else {
                val intent = Intent()
                    .putExtra(ARG_RESULT, UpgradeResult(plan.planName, billing))
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    companion object {
        const val ARG_INPUT = "arg.plansInput"
        const val ARG_RESULT = "arg.plansResult"
    }

}
