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
import android.text.method.LinkMovementMethod
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
//            planDetails.removeAllViews()
            planDetails.apply {
                cycle = selectedCycle
                currency = selectedCurrency
                planSelectionListener = { planId, planName, amount ->
                    selectPlanListener(SelectedPlan(planId, planName, amount == PRICE_ZERO, cycle, currency, amount))
                }
                planDetailsListItem = plan
            }
        },
        diffCallback = PlanDetailsListItem.DiffCallback,
        recyclable = false
    )

    lateinit var selectPlanListener: (SelectedPlan) -> Unit
    private var selectedCurrency: PlanCurrency
    private var selectedCycle: PlanCycle
    private var selectable: Boolean = false

    init {
        selectedCycle = PlanCycle.YEARLY
        selectedCurrency = PlanCurrency.CHF

        binding.apply {
            planListRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = plansAdapter
            }
            currencySpinner.selected { currencyPosition ->
                selectedCurrency = PlanCurrency.values()[currencyPosition]
                plansAdapter.notifyDataSetChanged()
            }

            billingCycleSpinner.selected { cyclePosition ->
                selectedCycle = PlanCycle.values()[cyclePosition]
                plansAdapter.notifyDataSetChanged()

                val renewInfoText = when (selectedCycle) {
                    PlanCycle.MONTHLY -> {
                        context.getString(R.string.plans_save_20)
                    }
                    PlanCycle.YEARLY -> {
                        context.getString(R.string.plans_renew_info)
                    }
                    PlanCycle.TWO_YEARS -> {
                        context.getString(R.string.plans_renew_info)
                    }
                }
                billingCycleDescriptionText.text = renewInfoText
            }
            customizableFeaturesText.movementMethod = LinkMovementMethod.getInstance()
        }

        ArrayAdapter.createFromResource(
            context,
            R.array.supported_currencies,
            R.layout.plan_spinner_item
        ).also { adapter ->
            binding.currencySpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            context,
            R.array.supported_billing_cycle,
            R.layout.plan_spinner_item
        ).also { adapter ->
            binding.billingCycleSpinner.adapter = adapter
            binding.billingCycleSpinner.setSelection(1)
        }
    }

    var plans: List<PlanDetailsListItem>? = null
        set(value) = with(binding) {
            value?.let { it ->
                selectable = it.any { plan ->
                    plan is PlanDetailsListItem.PaidPlanDetailsListItem && plan.selectable
                }
                val paidPlan = it.firstOrNull { plan -> plan is PlanDetailsListItem.PaidPlanDetailsListItem }
                selectedCurrency =
                    (paidPlan as? PlanDetailsListItem.PaidPlanDetailsListItem)?.currency ?: PlanCurrency.CHF
                currencySpinner.setSelection(PlanCurrency.values().indexOf(selectedCurrency))
                plansAdapter.submitList(it)
                binding.apply {
                    currencySpinner.visibility = if (!selectable) GONE else VISIBLE
                    billingCycleSpinner.visibility = if (!selectable) GONE else VISIBLE
                    customizableFeaturesLayout.visibility = if (!selectable) GONE else VISIBLE
                    billingCycleDescriptionText.visibility = if (!selectable) GONE else VISIBLE
                }
            } ?: run {
                plansAdapter.submitList(emptyList())
                binding.apply {
                    currencySpinner.visibility = GONE
                    billingCycleSpinner.visibility = GONE
                    customizableFeaturesLayout.visibility = GONE
                    billingCycleDescriptionText.visibility = GONE
                }
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
}
