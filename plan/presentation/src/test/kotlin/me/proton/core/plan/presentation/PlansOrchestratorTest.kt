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

package me.proton.core.plan.presentation

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.ui.StartDynamicSelectPlan
import me.proton.core.plan.presentation.ui.StartDynamicUpgradePlan
import org.junit.Before
import org.junit.Test

class PlansOrchestratorTest {

    private val userId = UserId("test")
    private val planInputShow = PlanInput(userId = userId.id, showSubscription = true)
    private val planInputHide = PlanInput(userId = userId.id, showSubscription = false)

    private val context = mockk<Context>()
    private val dynamicSelectPlanLauncher = mockk<ActivityResultLauncher<Unit>>(relaxed = true)
    private val dynamicUpgradePlanLauncher = mockk<ActivityResultLauncher<PlanInput>>(relaxed = true)
    private val caller = mockk<ActivityResultCaller>(relaxed = true) {
        every { registerForActivityResult(StartDynamicSelectPlan, any()) } returns dynamicSelectPlanLauncher
        every { registerForActivityResult(StartDynamicUpgradePlan, any()) } returns dynamicUpgradePlanLauncher
    }

    private lateinit var orchestrator: PlansOrchestrator

    @Before
    fun beforeEveryTest() {
        orchestrator = PlansOrchestrator(context)
    }

    @Test
    fun registerLaunchers() = runTest {
        // When
        orchestrator.register(caller)
        // Then
        verify { caller.registerForActivityResult(any<StartDynamicSelectPlan>(), any()) }
        verify { caller.registerForActivityResult(any<StartDynamicUpgradePlan>(), any()) }
    }

    @Test
    fun unregisterLaunchers() = runTest {
        // When
        orchestrator.register(caller)
        orchestrator.unregister()
        // Then
        verify { dynamicSelectPlanLauncher.unregister() }
        verify { dynamicUpgradePlanLauncher.unregister() }
    }

    @Test
    fun showCurrentPlanWorkflowLaunchDynamic() = runTest {
        // Given
        orchestrator.register(caller)
        // When
        orchestrator.showCurrentPlanWorkflow(userId)
        // Then
        verify(exactly = 0) { dynamicSelectPlanLauncher.launch(Unit) }
        verify(exactly = 1) { dynamicUpgradePlanLauncher.launch(planInputShow) }
    }

    @Test
    fun startUpgradeWorkflowLaunchDynamic() = runTest {
        // Given
        orchestrator.register(caller)
        // When
        orchestrator.startUpgradeWorkflow(userId)
        // Then
        verify(exactly = 0) { dynamicSelectPlanLauncher.launch(Unit) }
        verify(exactly = 1) { dynamicUpgradePlanLauncher.launch(planInputHide) }
    }

    @Test
    fun startSignUpPlanChooserWorkflowLaunchDynamic() = runTest {
        // Given
        orchestrator.register(caller)
        // When
        orchestrator.startSignUpPlanChooserWorkflow()
        // Then
        verify(exactly = 1) { dynamicSelectPlanLauncher.launch(Unit) }
        verify(exactly = 0) { dynamicUpgradePlanLauncher.launch(any()) }
    }
}
