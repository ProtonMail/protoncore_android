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

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.IsDynamicPlanEnabled
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.ui.StartDynamicSelectPlan
import me.proton.core.plan.presentation.ui.StartDynamicUpgradePlan
import me.proton.core.plan.presentation.ui.StartStaticUpgradePlan
import org.junit.Before
import org.junit.Test


class PlansOrchestratorTest {

    private val userId = UserId("test")
    private val planInputShow = PlanInput(userId = userId.id, showSubscription = true)
    private val planInputHide = PlanInput(userId = userId.id, showSubscription = false)
    private val planInputDefault = PlanInput()

    private val isDynamicPlanEnabled = mockk<IsDynamicPlanEnabled> {
        every { this@mockk.invoke(userId) } returns true
    }

    private val plansLauncher = mockk<ActivityResultLauncher<PlanInput>>(relaxed = true)
    private val dynamicSelectPlanLauncher = mockk<ActivityResultLauncher<Unit>>(relaxed = true)
    private val dynamicUpgradePlanLauncher = mockk<ActivityResultLauncher<PlanInput>>(relaxed = true)
    private val caller = mockk<ActivityResultCaller>(relaxed = true) {
        every { registerForActivityResult(StartStaticUpgradePlan, any()) } returns plansLauncher
        every { registerForActivityResult(StartDynamicSelectPlan, any()) } returns dynamicSelectPlanLauncher
        every { registerForActivityResult(StartDynamicUpgradePlan, any()) } returns dynamicUpgradePlanLauncher
    }

    private lateinit var orchestrator: PlansOrchestrator

    @Before
    fun beforeEveryTest() {
        orchestrator = PlansOrchestrator(isDynamicPlanEnabled)
    }

    @Test
    fun registerLaunchers() = runTest {
        // When
        orchestrator.register(caller)
        // Then
        verify { caller.registerForActivityResult(any<StartStaticUpgradePlan>(), any()) }
        verify { caller.registerForActivityResult(any<StartDynamicSelectPlan>(), any()) }
        verify { caller.registerForActivityResult(any<StartDynamicUpgradePlan>(), any()) }
    }

    @Test
    fun unregisterLaunchers() = runTest {
        // When
        orchestrator.register(caller)
        orchestrator.unregister()
        // Then
        verify { plansLauncher.unregister() }
        verify { dynamicSelectPlanLauncher.unregister() }
        verify { dynamicUpgradePlanLauncher.unregister() }
    }

    @Test
    fun showCurrentPlanWorkflowLaunchOld() = runTest {
        // Given
        every { isDynamicPlanEnabled.invoke(userId) } returns false
        orchestrator.register(caller)
        // When
        orchestrator.showCurrentPlanWorkflow(userId)
        // Then
        verify(exactly = 1) { plansLauncher.launch(planInputShow) }
        verify(exactly = 0) { dynamicSelectPlanLauncher.launch(Unit) }
        verify(exactly = 0) { dynamicUpgradePlanLauncher.launch(any()) }
    }

    @Test
    fun showCurrentPlanWorkflowLaunchDynamic() = runTest {
        // Given
        every { isDynamicPlanEnabled.invoke(userId) } returns true
        orchestrator.register(caller)
        // When
        orchestrator.showCurrentPlanWorkflow(userId)
        // Then
        verify(exactly = 0) { plansLauncher.launch(any()) }
        verify(exactly = 0) { dynamicSelectPlanLauncher.launch(Unit) }
        verify(exactly = 1) { dynamicUpgradePlanLauncher.launch(planInputShow) }
    }

    @Test
    fun startUpgradeWorkflowLaunchOld() = runTest {
        // Given
        every { isDynamicPlanEnabled.invoke(userId) } returns false
        orchestrator.register(caller)
        // When
        orchestrator.startUpgradeWorkflow(userId)
        // Then
        verify(exactly = 1) { plansLauncher.launch(planInputHide) }
        verify(exactly = 0) { dynamicSelectPlanLauncher.launch(Unit) }
        verify(exactly = 0) { dynamicUpgradePlanLauncher.launch(any()) }
    }

    @Test
    fun startUpgradeWorkflowLaunchDynamic() = runTest {
        // Given
        every { isDynamicPlanEnabled.invoke(userId) } returns true
        orchestrator.register(caller)
        // When
        orchestrator.startUpgradeWorkflow(userId)
        // Then
        verify(exactly = 0) { plansLauncher.launch(any()) }
        verify(exactly = 0) { dynamicSelectPlanLauncher.launch(Unit) }
        verify(exactly = 1) { dynamicUpgradePlanLauncher.launch(planInputHide) }
    }

    @Test
    fun startSignUpPlanChooserWorkflowLaunchOld() = runTest {
        // Given
        every { isDynamicPlanEnabled.invoke(userId = null) } returns false
        orchestrator.register(caller)
        // When
        orchestrator.startSignUpPlanChooserWorkflow()
        // Then
        verify(exactly = 1) { plansLauncher.launch(planInputDefault) }
        verify(exactly = 0) { dynamicSelectPlanLauncher.launch(Unit) }
        verify(exactly = 0) { dynamicUpgradePlanLauncher.launch(any()) }
    }

    @Test
    fun startSignUpPlanChooserWorkflowLaunchDynamic() = runTest {
        // Given
        every { isDynamicPlanEnabled.invoke(userId = null) } returns true
        orchestrator.register(caller)
        // When
        orchestrator.startSignUpPlanChooserWorkflow()
        // Then
        verify(exactly = 0) { plansLauncher.launch(planInputDefault) }
        verify(exactly = 1) { dynamicSelectPlanLauncher.launch(Unit) }
        verify(exactly = 0) { dynamicUpgradePlanLauncher.launch(any()) }
    }
}
