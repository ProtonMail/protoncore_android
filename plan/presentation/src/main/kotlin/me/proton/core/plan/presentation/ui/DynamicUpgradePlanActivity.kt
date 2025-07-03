/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.plan.presentation.ui

import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.presentation.databinding.ActivityDynamicUpgradePlanBinding
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.entity.UpgradeResult
import me.proton.core.plan.presentation.entity.DynamicUser
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.utils.enableProtonEdgeToEdge

@AndroidEntryPoint
class DynamicUpgradePlanActivity : ProtonViewBindingActivity<ActivityDynamicUpgradePlanBinding>(
    ActivityDynamicUpgradePlanBinding::inflate
) {
    private val planUpgrade by lazy { binding.planUpgrade.getFragment<DynamicUpgradePlanFragment>() }

    private val input: PlanInput? by lazy { intent?.extras?.getParcelable(ARG_INPUT) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableProtonEdgeToEdge()
        super.onCreate(savedInstanceState)
        planUpgrade.setOnBackClicked { finish() }
        planUpgrade.setOnPlanBilled { plan, result -> setResultAndFinish(plan, result) }
    }

    override fun onResume() {
        super.onResume()
        planUpgrade.setUser(input?.user?.let { DynamicUser.ByUserId(it) } ?: DynamicUser.Primary)
        planUpgrade.setShowSubscription(input?.showSubscription ?: true)
    }

    private fun setResultAndFinish(plan: SelectedPlan, result: BillingResult) {
        val intent = Intent().apply {
            putExtra(ARG_RESULT, UpgradeResult(plan.planName, plan.planDisplayName, result))
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        const val ARG_INPUT = "arg.plansInput"
        const val ARG_RESULT = "arg.plansResult"
    }
}
