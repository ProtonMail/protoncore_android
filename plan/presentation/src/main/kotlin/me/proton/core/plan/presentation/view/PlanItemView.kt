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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.PlanItemBinding
import me.proton.core.plan.presentation.entity.PlanCurrency
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import me.proton.core.presentation.utils.PRICE_ZERO
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.exhaustive
import java.text.SimpleDateFormat
import java.util.Locale

class PlanItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    internal val binding = PlanItemBinding.inflate(LayoutInflater.from(context), this, true)
    internal var collapsible: Boolean = true

    var planSelectionListener: ((String, String, Double) -> Unit)? = null
    var billableAmount = PRICE_ZERO

    private lateinit var currency: PlanCurrency
    private lateinit var cycle: PlanCycle
    private lateinit var planDetailsItem: PlanDetailsItem

    fun setData(plan: PlanDetailsItem, cycle: PlanCycle, currency: PlanCurrency?, collapsible: Boolean = true) {
        this.planDetailsItem = plan
        this.cycle = cycle
        this.currency = currency ?: PlanCurrency.EUR
        this.collapsible = collapsible

        initCommonViews(plan)
        when (plan) {
            is PlanDetailsItem.FreePlanDetailsItem -> bindFreePlan(plan)
            is PlanDetailsItem.PaidPlanDetailsItem -> bindPaidPlan(plan)
            is PlanDetailsItem.CurrentPlanDetailsItem -> bindCurrentPlan(plan)
        }.exhaustive
    }

    private fun initCommonViews(plan: PlanDetailsItem) = with(binding) {
        planNameText.text = plan.displayName
        planGroup.visibility = if (!collapsible) VISIBLE else GONE
        collapse.apply {
            visibility = if (collapsible) VISIBLE else GONE
            onClick { rotate() }
        }
        select.onClick {
            planSelectionListener?.invoke(plan.name, plan.displayName, billableAmount)
        }
    }

    private fun bindCurrentPlan(plan: PlanDetailsItem.CurrentPlanDetailsItem) = with(binding) {
        currentPlanGroup.visibility = VISIBLE
        storageProgress.apply {
            setIndicatorColor(ContextCompat.getColor(context, R.color.green_pea))
            progress = plan.progressValue
        }
        storageText.text = formatUsedSpace(context, plan.usedSpace, plan.maxSpace)

        planDescriptionText.text = context.getString(R.string.plans_current_plan)
        planPercentageText.visibility = GONE
        select.visibility = GONE
        val featureOrder = context.getStringArrayByName(R.array.plan_current_order)
        context.getIntegerArrayByName(R.array.plan_current)?.let {
            bindPlanFeatures(
                length = it.length()
            ) { index: Int ->
                createCurrentPlanFeature(featureOrder!![index - 1], it, index - 1, context, plan)
            }
            it.recycle()
        }

        when (cycle) {
            PlanCycle.FREE -> planCycleText.visibility = GONE
            PlanCycle.MONTHLY -> planCycleText.text = context.getString(R.string.plans_billing_monthly)
            PlanCycle.YEARLY -> planCycleText.text = context.getString(R.string.plans_billing_yearly)
            PlanCycle.TWO_YEARS -> planCycleText.text = context.getString(R.string.plans_billing_two_years)
        }.exhaustive
        val renewalInfoText = if (plan.isAutoRenewal) R.string.plans_renewal_date else R.string.plans_expiration_date
        plan.endDate?.let {
            planRenewalText.apply {
                text = HtmlCompat.fromHtml(
                    String.format(
                        context.getString(renewalInfoText),
                        SimpleDateFormat(RENEWAL_DATE_FORMAT, Locale.getDefault()).format(it)
                    ),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                visibility = View.VISIBLE
            }
            separator.visibility = View.VISIBLE
        }

        val amount = plan.price?.let { price -> cycle.getPrice(price) } ?: PRICE_ZERO
        billableAmount = amount
        planPriceText.text = amount.formatCentsPriceDefaultLocale(currency.name)
    }

    private fun bindFreePlan(plan: PlanDetailsItem.FreePlanDetailsItem) = with(binding) {
        select.text = context.getString(R.string.plans_proton_for_free)
        planCycleText.visibility = View.GONE
        planPriceText.text = PRICE_ZERO.formatCentsPriceDefaultLocale(currency.name)
        val featureOrder = context.getStringArrayByName("plan_id_${plan.name}_order")

        context.getIntegerArrayByName("plan_id_${plan.name}")?.let {
            bindPlanFeatures(
                length = it.length().minus(1)
            ) { index: Int ->
                createPlanFeature(featureOrder!![index - 1], it, index, context, plan)
            }
            binding.planDescriptionText.text = context.getString(it.getResourceId(0, 0))
            it.recycle()
        }
    }

    private fun bindPaidPlan(plan: PlanDetailsItem.PaidPlanDetailsItem) = with(binding) {
        select.text = String.format(context.getString(R.string.plans_get_proton), plan.displayName)
        starred.visibility = if (plan.starred) VISIBLE else INVISIBLE
        val featureOrder = context.getStringArrayByName("plan_id_${plan.name}_order")
        context.getIntegerArrayByName("plan_id_${plan.name}")?.let {
            bindPlanFeatures(
                length = it.length().minus(1)
            ) { index: Int ->
                createPlanFeature(featureOrder!![index - 1], it, index, context, plan)
            }
            binding.planDescriptionText.text = context.getString(it.getResourceId(0, 0))
            it.recycle()
        }

        val maxMonthlyPrice = PlanCycle.MONTHLY.getPrice(plan.price) ?: PRICE_ZERO
        when (cycle) {
            PlanCycle.MONTHLY,
            PlanCycle.FREE -> planPriceDescriptionText.visibility = GONE
            PlanCycle.YEARLY,
            PlanCycle.TWO_YEARS -> planPriceDescriptionText.visibility = VISIBLE
        }.exhaustive
        calculatePaidPlanPrice(plan = plan, maxMonthlyPrice = maxMonthlyPrice)
    }

    private fun bindPlanFeatures(
        length: Int,
        createFeatureItem: (Int) -> Pair<String, Int>
    ) = with(binding) {
        planContents.removeAllViews()
        for (i in 1..length) {
            planContents.addView(
                PlanContentItemView(context).apply {
                    val planItems = createFeatureItem(i)
                    planItemText = planItems.first
                    planItemIcon = planItems.second
                }
            )
        }
    }

    private fun calculatePaidPlanPrice(plan: PlanDetailsItem.PaidPlanDetailsItem, maxMonthlyPrice: Double) =
        with(binding) {
            val amount = plan.price.let { price -> cycle.getPrice(price) } ?: PRICE_ZERO

            val monthlyPrice = calculateMonthlyPrice(amount)
            if (amount != PRICE_ZERO) {
                val discount = (maxMonthlyPrice - monthlyPrice) / maxMonthlyPrice * 100
                planPercentageText.visibility = if (discount > 0) VISIBLE else GONE
                planPercentageText.text = "(-${discount.toInt()}%)"
            }
            val price = monthlyPrice.formatCentsPriceDefaultLocale(currency.name)
            planPriceText.text = price

            planCycleText.visibility = VISIBLE
            planPriceDescriptionText.text = String.format(
                context.getString(R.string.plans_billed_yearly),
                (monthlyPrice * MONTHS_IN_YEAR).formatCentsPriceDefaultLocale(currency.name, fractionDigits = 2)
            )
            billableAmount = amount
        }

    private fun calculateMonthlyPrice(amount: Double) = when (cycle) {
        PlanCycle.MONTHLY -> amount
        PlanCycle.YEARLY -> amount / MONTHS_IN_YEAR
        PlanCycle.TWO_YEARS -> amount / MONTHS_IN_2YEARS
        PlanCycle.FREE -> PRICE_ZERO
    }.exhaustive.toDouble()

    companion object {
        private const val MONTHS_IN_YEAR = 12
        private const val MONTHS_IN_2YEARS = 24
        private const val RENEWAL_DATE_FORMAT = "MMM dd, yyyy"
    }
}
