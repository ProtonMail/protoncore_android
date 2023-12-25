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

package me.proton.core.payment.presentation.view

import android.view.ViewGroup
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.proton.core.domain.type.IntEnum
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentButtonViewModel
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanState
import me.proton.core.plan.domain.entity.DynamicPlanType
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.UnconfinedCoroutinesTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test

class ProtonPaymentButtonTest : CoroutinesTest by UnconfinedCoroutinesTest() {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @MockK(relaxed = true)
    private lateinit var viewModel: ProtonPaymentButtonViewModel

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `initial empty state`() {
        val view = makeButton(viewModel, withParams = false)
        paparazzi.snapshot(view)
    }

    @Test
    fun `mail plus plan with giap provider`() {
        val view = makeButton(viewModel)
        paparazzi.snapshot(view)
    }

    @Test
    fun `loading state`() {
        every { viewModel.buttonStates(1) } returns
            MutableStateFlow(ProtonPaymentButtonViewModel.ButtonState.Loading).asStateFlow()
        val view = makeButton(viewModel, id = 1)
        paparazzi.snapshot(view)
    }

    @Test
    fun `disabled state`() {
        every { viewModel.buttonStates(1) } returns
            MutableStateFlow(ProtonPaymentButtonViewModel.ButtonState.Disabled).asStateFlow()
        val view = makeButton(viewModel, id = 1)
        paparazzi.snapshot(view, offsetMillis = 1000)
    }

    private fun makeButton(
        viewModel: ProtonPaymentButtonViewModel,
        id: Int? = null,
        withParams: Boolean = true
    ): ProtonPaymentButton {
        val view = ProtonPaymentButton(paparazzi.context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.testViewModel = viewModel
        id?.let { view.id = it }
        if (withParams) {
            view.apply {
                currency = "CHF"
                cycle = 12
                paymentProvider = PaymentProvider.GoogleInAppPurchase
                plan = mailPlusPlan
                userId = null
            }
        }
        return view
    }
}

val mailPlusPlan = DynamicPlan(
    name = "mail2022",
    order = 1,
    state = DynamicPlanState.Available,
    title = "Mail Plus",
    type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary),
)
