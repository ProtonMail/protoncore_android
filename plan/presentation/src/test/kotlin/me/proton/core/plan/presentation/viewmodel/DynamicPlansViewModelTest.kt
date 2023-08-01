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

package me.proton.core.plan.presentation.viewmodel

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.usecase.GetDynamicPlans
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DynamicPlansViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var getDynamicPlans: GetDynamicPlans

    private lateinit var tested: DynamicPlansViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = DynamicPlansViewModel(getDynamicPlans)
    }

    @Test
    fun `get plans happy path`() = coroutinesTest {
        // GIVEN
        val plans = listOf(mockk<DynamicPlan>())
        coEvery { getDynamicPlans(any()) } returns plans

        // WHEN
        tested.loadPlans(UserId("user_id")).join()

        // THEN
        val state = assertIs<DynamicPlansViewModel.State.PlansLoaded>(tested.state.value)
        assertContentEquals(plans, state.plans)
    }

    @Test
    fun `get plans error`() = coroutinesTest {
        // GIVEN
        val apiException = ApiException(ApiResult.Error.Http(500, "Server error"))
        coEvery { getDynamicPlans(any()) } throws apiException

        // WHEN
        tested.loadPlans(UserId("user_id")).join()

        // THEN
        val state = assertIs<DynamicPlansViewModel.State.Error>(tested.state.value)
        assertEquals(apiException, state.throwable)
    }
}
