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
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.UnredeemedPurchaseResult
import me.proton.core.plan.presentation.entity.UpgradeResult

object StartDynamicUpgradePlan : ActivityResultContract<PlanInput, UpgradeResult?>() {
    override fun createIntent(context: Context, input: PlanInput): Intent =
        Intent(context, DynamicUpgradePlanActivity::class.java).apply {
            putExtra(DynamicUpgradePlanActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): UpgradeResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(DynamicUpgradePlanActivity.ARG_RESULT)
    }
}

object StartDynamicSelectPlan : ActivityResultContract<Unit, Unit?>() {
    override fun createIntent(context: Context, input: Unit) = Intent(context, DynamicSelectPlanActivity::class.java)
    override fun parseResult(resultCode: Int, intent: Intent?): Unit? = null
}

object StartUnredeemedPurchase : ActivityResultContract<Unit, UnredeemedPurchaseResult?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(context, UnredeemedPurchaseActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): UnredeemedPurchaseResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(UnredeemedPurchaseActivity.ARG_RESULT)
    }
}
