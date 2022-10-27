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
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.PlanListViewItemBinding
import me.proton.core.plan.presentation.databinding.PlansListViewBinding
import me.proton.core.plan.presentation.entity.PlanCurrency
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.entity.SubscribedPlan
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
            planDetails.apply {
                setData(
                    SubscribedPlan(
                        plan = plan, amount = null, currency = selectedCurrency, collapsible = plansSize != 1
                    )
                )

                planSelectionListener = { planId, planName, amount, services, type ->
                    selectPlanListener(
                        SelectedPlan(
                            planId,
                            planName,
                            amount == PRICE_ZERO,
                            selectedCycle,
                            selectedCurrency,
                            amount,
                            services,
                            type,
                            vendorNames = (plan as? PlanDetailsItem.PaidPlanDetailsItem)?.vendors ?: emptyMap()
                        )
                    )
                }
                setBackgroundResource(R.drawable.background_plan_list_item)
            }
        },
        diffCallback = PlanDetailsItem.DiffCallback,
        recyclable = false
    )

    lateinit var selectPlanListener: (SelectedPlan) -> Unit
    private var selectedCurrency: PlanCurrency
    private var selectedCycle: PlanCycle

    init {
        selectedCurrency = PlanCurrency.EUR
        selectedCycle = PlanCycle.YEARLY

        binding.apply {
            planListRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = plansAdapter
            }
            currencySpinner.selected { currencyPosition ->
                selectedCurrency = PlanCurrency.values()[currencyPosition]
                plansAdapter.notifyDataSetChanged()
            }
        }

        ArrayAdapter.createFromResource(
            context,
            R.array.supported_currencies,
            R.layout.plan_spinner_item
        ).also { adapter ->
            binding.currencySpinner.adapter = adapter
        }
    }

    private var plansSize: Int = 0

    var purchaseEnabled: Boolean = true
    var plans: List<PlanDetailsItem>? = null
        set(value) = with(binding) {
            value?.let {
                val paidPlan = it.firstOrNull { plan -> plan is PlanDetailsItem.PaidPlanDetailsItem }
                selectedCurrency =
                    (paidPlan as? PlanDetailsItem.PaidPlanDetailsItem)?.currency ?: PlanCurrency.CHF
                currencySpinner.apply {
                    setSelection(PlanCurrency.values().indexOf(selectedCurrency))
                }
                plansSize = it.size

                setSpinnersVisibility(if (purchaseEnabled) View.VISIBLE else View.GONE)
                plansAdapter.submitList(
                    it.map { plan ->
                        if (plan is PlanDetailsItem.PaidPlanDetailsItem)
                            plan.copy(purchaseEnabled = purchaseEnabled)
                        else plan
                    }
                )
            } ?: run {
                plansAdapter.submitList(emptyList())
                setSpinnersVisibility(View.GONE)
            }
        }

    private fun Spinner.selected(action: (Int) -> Unit) {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                action(position)
            }
        }
    }

    private fun setSpinnersVisibility(visibility: Int) = with(binding) {
        currencySpinner.visibility = visibility
    }
}
