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

package me.proton.core.plan.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import me.proton.core.plan.presentation.databinding.PlanListViewItemBinding
import me.proton.core.plan.presentation.databinding.PlansListViewBinding
import me.proton.core.plan.presentation.entity.PlanDetailsListItem
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.presentation.ui.adapter.ProtonAdapter
import me.proton.core.presentation.utils.PRICE_ZERO

internal class PlansListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = PlansListViewBinding.inflate(LayoutInflater.from(context), this, true)

    private val plansAdapter = ProtonAdapter(
        getView = { parent, inflater -> PlanListViewItemBinding.inflate(inflater, parent, false) },
        onBind = { plan ->
            planDetails.planDetailsListItem = plan
            planDetails.planSelectionListener = { planName, planDisplayName, cycle, currency, amount ->
                selectPlanListener(SelectedPlan(planName, planDisplayName, amount == PRICE_ZERO, cycle, currency, amount))
            }
        },
        diffCallback = PlanDetailsListItem.DiffCallback
    )

    lateinit var selectPlanListener: (SelectedPlan) -> Unit

    init {
        binding.planListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = plansAdapter
        }
    }

    var plans: List<PlanDetailsListItem>? = null
        set(value) = with(binding) {
            value?.let {
                plansAdapter.submitList(it)
            } ?: run {
                plansAdapter.submitList(emptyList())
            }
        }
}
