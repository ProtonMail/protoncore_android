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

package me.proton.core.plan.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.IsDynamicPlanEnabled
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.UpgradeResult
import me.proton.core.plan.presentation.ui.StartDynamicSelectPlan
import me.proton.core.plan.presentation.ui.StartDynamicUpgradePlan
import me.proton.core.plan.presentation.ui.StartStaticUpgradePlan
import javax.inject.Inject

@Suppress("TooManyFunctions")
class PlansOrchestrator @Inject constructor(
    private val isDynamicPlanEnabled: IsDynamicPlanEnabled
) {

    private var plansLauncher: ActivityResultLauncher<PlanInput>? = null
    private var dynamicSelectPlanLauncher: ActivityResultLauncher<Unit>? = null
    private var dynamicUpgradePlanLauncher: ActivityResultLauncher<PlanInput>? = null

    private var onUpgradeResultListener: ((result: UpgradeResult?) -> Unit)? = {}

    fun setOnUpgradeResult(block: (result: UpgradeResult?) -> Unit) {
        onUpgradeResultListener = block
    }

    private fun registerPlanResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<PlanInput> =
        caller.registerForActivityResult(
            StartStaticUpgradePlan
        ) {
            onUpgradeResultListener?.invoke(it)
        }

    private fun registerDynamicSelectPlanResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<Unit> =
        caller.registerForActivityResult(
            StartDynamicSelectPlan
        ) { /* Unused */ }

    private fun registerDynamicUpgradePlanResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<PlanInput> =
        caller.registerForActivityResult(
            StartDynamicUpgradePlan
        ) {
            onUpgradeResultListener?.invoke(it)
        }

    private fun launchSelectPlanWorkflow() {
        when (isDynamicPlanEnabled(userId = null)) {
            false -> checkRegistered(plansLauncher).launch(PlanInput())
            true -> checkRegistered(dynamicSelectPlanLauncher).launch(Unit)
        }
    }

    private fun launchUpgradeWorkflow(userId: UserId, showSubscription: Boolean) {
        val planInput = PlanInput(userId = userId.id, showSubscription = showSubscription)
        when (isDynamicPlanEnabled(userId)) {
            false -> checkRegistered(plansLauncher).launch(planInput)
            true -> checkRegistered(dynamicUpgradePlanLauncher).launch(planInput)
        }
    }

    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(caller: ActivityResultCaller) {
        plansLauncher = registerPlanResult(caller)
        dynamicSelectPlanLauncher = registerDynamicSelectPlanResult(caller)
        dynamicUpgradePlanLauncher = registerDynamicUpgradePlanResult(caller)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        plansLauncher?.unregister()
        plansLauncher = null
        dynamicSelectPlanLauncher?.unregister()
        dynamicSelectPlanLauncher = null
        dynamicUpgradePlanLauncher?.unregister()
        dynamicUpgradePlanLauncher = null
    }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call PlansOrchestrator.register(context) before starting workflow!" }

    /**
     * Starts the Plan Chooser workflow (sign up or upgrade).
     *
     * @see [onUpgradeResult]
     */
    fun startSignUpPlanChooserWorkflow() {
        launchSelectPlanWorkflow()
    }

    /**
     * Starts the Plan Upgrade workflow (+current subscription).
     *
     * @see [onUpgradeResult]
     */
    fun showCurrentPlanWorkflow(userId: UserId) {
        launchUpgradeWorkflow(userId, showSubscription = true)
    }

    /**
     * Starts the Plan Upgrade workflow (without current subscription).
     *
     * @see [onUpgradeResult]
     */
    fun startUpgradeWorkflow(userId: UserId) {
        launchUpgradeWorkflow(userId, showSubscription = false)
    }
}

fun PlansOrchestrator.onUpgradeResult(
    block: (result: UpgradeResult?) -> Unit
): PlansOrchestrator {
    setOnUpgradeResult { block(it) }
    return this
}
