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
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.UpgradeResult
import me.proton.core.plan.presentation.ui.StartPlanChooser
import javax.inject.Inject

class PlansOrchestrator @Inject constructor() {

    private var plansLauncher: ActivityResultLauncher<PlanInput>? = null

    private var onPlanResultListener: ((result: UpgradeResult?) -> Unit)? = {}

    fun setOnPlanResult(block: (result: UpgradeResult?) -> Unit) {
        onPlanResultListener = block
    }

    private fun registerPlanResult(
        context: ComponentActivity
    ): ActivityResultLauncher<PlanInput> =
        context.registerForActivityResult(
            StartPlanChooser()
        ) {
            onPlanResultListener?.invoke(it)
        }

    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(context: ComponentActivity) {
        plansLauncher = registerPlanResult(context)
    }

    /**
     * Unregister all workflow activity launcher and listener.
     */
    fun unregister() {
        plansLauncher?.unregister()
        plansLauncher = null
    }

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call authOrchestrator.register(context) before starting workflow!" }

    /**
     * Starts the Plan Chooser workflow (sign up or upgrade).
     *
     * @see [onUpgradeResult]
     */
    fun startSignUpPlanChooserWorkflow() {
        checkRegistered(plansLauncher).launch(
            PlanInput()
        )
    }

    /**
     * Starts the Plan Chooser workflow (sign up or upgrade).
     *
     * @see [onUpgradeResult]
     */
    fun showCurrentPlanWorkflow(userId: UserId) {
        checkRegistered(plansLauncher).launch(
            PlanInput(userId = userId.id, showCurrent = true)
        )
    }

    /**
     * Starts the Plan Chooser workflow (sign up or upgrade).
     *
     * @see [onUpgradeResult]
     */
    fun startUpgradeWorkflow(userId: UserId) {
        checkRegistered(plansLauncher).launch(
            PlanInput(userId = userId.id, showCurrent = false)
        )
    }
}

fun PlansOrchestrator.onUpgradeResult(
    block: (result: UpgradeResult?) -> Unit
): PlansOrchestrator {
    setOnPlanResult { block(it) }
    return this
}
