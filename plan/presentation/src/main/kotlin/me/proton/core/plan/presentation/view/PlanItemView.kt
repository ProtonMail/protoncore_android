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
import android.content.res.Resources
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.bold
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.PlanItemBinding
import me.proton.core.plan.presentation.entity.Currency
import me.proton.core.plan.presentation.entity.Cycle
import me.proton.core.plan.presentation.entity.PlanDetailsListItem
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.exhaustive
import java.util.Locale

class PlanItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = PlanItemBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        with(binding) {
            currencySpinner.selected { currencyPosition ->
                selectedCurrency = Currency.values()[currencyPosition]
                calculateAndUpdatePriceUI()
            }
            billingCycleSpinner.selected { cyclePosition ->
                selectedCycle = Cycle.values()[cyclePosition]
                planDetailsListItem.let {
                    billableAmount = when (it) {
                        is PlanDetailsListItem.FreePlanDetailsListItem -> 0
                        is PlanDetailsListItem.PaidPlanDetailsListItem -> {
                            val planPricing = it.price
                            planPricing?.let { price ->
                                selectedCycle.getPrice(price)
                            } ?: 0
                        }
                        null -> 0
                    }.exhaustive
                }
                calculateAndUpdatePriceUI()
            }

            selectPlan.onClick {
                planSelectionListener(
                    planId,
                    planName,
                    selectedCycle,
                    selectedCurrency,
                    billableAmount
                )
            }
        }
    }

    lateinit var planSelectionListener: (String, String, Cycle, Currency, Int) -> Unit

    private var selectedCurrency: Currency = Currency.EUR
    private var selectedCycle: Cycle = Cycle.YEARLY
    private var billableAmount: Int = 0
    private lateinit var planId: String
    private lateinit var planName: String

    var planDetailsListItem: PlanDetailsListItem? = null
        set(value) {
            value?.let { plan ->
                field = value
                planId = plan.id
                when (plan) {
                    is PlanDetailsListItem.FreePlanDetailsListItem -> bindFreePlan(plan)
                    is PlanDetailsListItem.PaidPlanDetailsListItem -> bindPaidPlan(plan)
                }.exhaustive
            }
        }

    private fun bindFreePlan(plan: PlanDetailsListItem.FreePlanDetailsListItem) = with(binding) {
        planName = context.getString(R.string.plans_free_name)
        planNameText.text = planName
        if (plan.current) {
            planCycleText.text = context.getString(R.string.plans_current_plan)
        } else {
            planCycleText.visibility = View.GONE
        }
        planPriceText.visibility = View.GONE
        billableAmount = 0

        resources.getStringArray(R.array.free).forEach { item ->
            planContents.addView(PlanContentItemView(context).apply {
                planItem = item
            })
        }

        currencySpinner.visibility = GONE
        billingCycleSpinner.visibility = GONE

        if (!plan.selectable) {
            selectPlan.visibility = GONE
        }
    }

    private fun bindPaidPlan(plan: PlanDetailsListItem.PaidPlanDetailsListItem) = with(binding) {
        planName = plan.name.capitalize(Locale.getDefault())
        planNameText.text = planName
        plan.renewalDate?.let {
            planRenewalText.apply {
                text = SpannableStringBuilder(context.getString(R.string.plans_renewal_date))
                    .bold { append(" $it") }
                visibility = View.VISIBLE
            }
            separator.visibility = View.VISIBLE
        }
        if (plan.current) {
            planCycleText.text = context.getString(R.string.plans_current_plan)
            planPriceText.visibility = View.GONE
        }
        ArrayAdapter.createFromResource(
            context,
            R.array.supported_currencies,
            R.layout.plan_spinner_item
        ).also { adapter ->
            currencySpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            context,
            R.array.supported_billing_cycle,
            R.layout.plan_spinner_item
        ).also { adapter ->
            billingCycleSpinner.adapter = adapter
            billingCycleSpinner.setSelection(1)
            billableAmount = plan.price?.yearly ?: 0
        }

        val planContentsList = context.getStringArrayByName(plan.name)
        planContentsList?.forEach { item ->
            planContents.addView(PlanContentItemView(context).apply {
                planItem = item
            })
        }

        if (!plan.selectable) {
            selectPlan.visibility = GONE
            currencySpinner.visibility = GONE
            billingCycleSpinner.visibility = GONE
        }
        if (plan.upgrade) {
            selectPlan.text = context.getString(R.string.plans_upgrade_plan)
        }
    }

    private fun calculateAndUpdatePriceUI() = with(binding) {
        planCycleText.visibility = VISIBLE
        val monthlyPrice: Double = when (selectedCycle) {
            Cycle.MONTHLY -> billableAmount
            Cycle.YEARLY -> billableAmount / 12
        }.exhaustive.toDouble()

        planPriceText.text = when (selectedCurrency) {
            Currency.EUR -> String.format("%.2f%s", monthlyPrice / 100, selectedCurrency.sign)
            Currency.USD -> String.format("%s%.2f", selectedCurrency.sign, monthlyPrice / 100)
            Currency.CHF -> String.format("%.2f%s", monthlyPrice / 100, selectedCurrency.sign)
        }.exhaustive

        planPriceDescriptionText.text = when (selectedCurrency) {
            Currency.EUR -> String.format(
                context.getString(R.string.plans_billed_yearly_eur),
                billableAmount / 100,
                selectedCurrency.sign
            )
            Currency.USD -> String.format(
                context.getString(R.string.plans_billed_yearly_usd),
                selectedCurrency.sign,
                billableAmount / 100
            )
            Currency.CHF -> String.format(
                context.getString(R.string.plans_billed_yearly_chf),
                billableAmount / 100,
                selectedCurrency.sign
            )
        }.exhaustive
    }
}

fun Spinner.selected(action: (Int) -> Unit) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            action(position)
        }
    }
}

private fun Context.getStringArrayByName(aString: String) =
    try {
        resources.getStringArray(resources.getIdentifier(aString, "array", packageName))
    } catch (notFound: Resources.NotFoundException) {
        null
    }
