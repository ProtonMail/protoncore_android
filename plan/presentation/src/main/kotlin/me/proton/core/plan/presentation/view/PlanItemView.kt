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
import me.proton.core.plan.domain.entity.PLAN_PRODUCT
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.PlanItemBinding
import me.proton.core.plan.presentation.entity.PlanCurrency
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import me.proton.core.presentation.utils.PRICE_ZERO
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.exhaustive
import java.text.DateFormat

class PlanItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    internal val binding = PlanItemBinding.inflate(LayoutInflater.from(context), this, true)
    internal var collapsible: Boolean = true

    var planSelectionListener: ((String, String, Double, Int, Int) -> Unit)? = null
    var billableAmount = PRICE_ZERO

    private lateinit var currency: PlanCurrency
    private lateinit var cycle: PlanCycle
    private lateinit var planDetailsItem: PlanDetailsItem

    fun setData(
        plan: PlanDetailsItem,
        cycle: PlanCycle = PlanCycle.YEARLY,
        currency: PlanCurrency?,
        collapsible: Boolean = true
    ) {
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
        planItemParent.onClick {
            rotate()
        }
    }

    private fun bindCurrentPlan(plan: PlanDetailsItem.CurrentPlanDetailsItem) = with(binding) {
        currentPlanGroup.visibility = VISIBLE
        storageProgress.apply {
            val usedPercentage = plan.usedSpace.toDouble() / plan.maxSpace
            val indicatorColor = when {
                usedPercentage < 0.5 -> ContextCompat.getColor(context, R.color.notification_success)
                usedPercentage < 0.9 -> ContextCompat.getColor(context, R.color.notification_warning)
                else -> ContextCompat.getColor(context, R.color.notification_error)
            }
            setIndicatorColor(indicatorColor)
            progress = plan.progressValue
        }
        storageText.text = formatUsedSpace(context, plan.usedSpace, plan.maxSpace)

        planDescriptionText.text = context.getString(R.string.plans_current_plan)
        planPercentageText.visibility = GONE
        select.visibility = GONE
        val featureOrder = context.getStringArrayByName(R.array.plan_current_order)
        val featureIcons = context.getIntegerArrayByName(R.array.plan_current_icons)
        context.getIntegerArrayByName(R.array.plan_current)?.let {
            bindPlanFeatures(
                length = it.length()
            ) { index: Int ->
                createCurrentPlanFeature(
                    featureOrder!![index - 1],
                    featureIcons?.getResourceId(index - 1, 0) ?: 0,
                    it,
                    index - 1,
                    context,
                    plan
                )
            }
            featureIcons?.recycle()
            it.recycle()
        }

        when (plan.cycle) {
            PlanCycle.MONTHLY -> planCycleText.text = context.getString(R.string.plans_billing_monthly)
            PlanCycle.YEARLY -> planCycleText.text = context.getString(R.string.plans_billing_yearly)
            PlanCycle.TWO_YEARS -> planCycleText.text = context.getString(R.string.plans_billing_two_years)
            null, PlanCycle.FREE -> planCycleText.visibility = GONE
            PlanCycle.OTHER -> planCycleText.text =
                String.format(context.getString(R.string.plans_billing_other_period), plan.cycle.cycleDurationMonths)
        }.exhaustive
        val renewalInfoText = if (plan.isAutoRenewal) R.string.plans_renewal_date else R.string.plans_expiration_date
        plan.endDate?.let {
            planRenewalText.apply {
                text = HtmlCompat.fromHtml(
                    String.format(
                        context.getString(renewalInfoText),
                        DateFormat.getDateInstance().format(it)
                    ),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                visibility = View.VISIBLE
            }
            separator.visibility = View.VISIBLE
        }

        val amount = plan.price?.let { price -> plan.cycle?.getPrice(price) } ?: PRICE_ZERO
        billableAmount = amount
        planPriceText.text = amount.formatCentsPriceDefaultLocale(currency.name)
    }

    private fun bindFreePlan(plan: PlanDetailsItem.FreePlanDetailsItem) = with(binding) {
        select.text = context.getString(R.string.plans_proton_for_free)
        planCycleText.visibility = View.GONE
        planPriceText.text = PRICE_ZERO.formatCentsPriceDefaultLocale(currency.name)
        val featureOrder = context.getStringArrayByName("plan_id_${plan.name}_order")
        val featureIcons = context.getIntegerArrayByName("plan_id_${plan.name}_icons")

        context.getIntegerArrayByName("plan_id_${plan.name}")?.let {
            bindPlanFeatures(
                length = it.length().minus(1)
            ) { index: Int ->
                createPlanFeature(
                    featureOrder!![index - 1],
                    featureIcons?.getResourceId(index - 1, 0) ?: 0,
                    it,
                    index,
                    context,
                    plan
                )
            }
            binding.planDescriptionText.text = context.getString(it.getResourceId(0, 0))
            featureIcons?.recycle()
            it.recycle()
        }
        select.onClick {
            planSelectionListener?.invoke(plan.name, plan.displayName, billableAmount, 0, PLAN_PRODUCT)
        }
    }

    private fun bindPaidPlan(plan: PlanDetailsItem.PaidPlanDetailsItem) = with(binding) {
        select.text = String.format(context.getString(R.string.plans_get_proton), plan.displayName)
        starred.visibility = if (plan.starred) VISIBLE else INVISIBLE
        val featureOrder = context.getStringArrayByName("plan_id_${plan.name}_order")
        val featureIcons = context.getIntegerArrayByName("plan_id_${plan.name}_icons")
        context.getIntegerArrayByName("plan_id_${plan.name}")?.let {
            bindPlanFeatures(
                length = it.length().minus(1)
            ) { index: Int ->
                createPlanFeature(
                    featureOrder!![index - 1],
                    featureIcons?.getResourceId(index - 1, 0) ?: 0,
                    it,
                    index,
                    context,
                    plan
                )
            }
            binding.planDescriptionText.text = context.getString(it.getResourceId(0, 0))
            it.recycle()
            featureIcons?.recycle()
        }

        val maxPrice = cycle.getPrice(plan.price) ?: PRICE_ZERO
        calculatePaidPlanPrice(plan = plan, maxPrice = maxPrice)

        if (!plan.purchaseEnabled) {
            select.visibility = GONE
            priceCycleLayout.visibility = GONE
        }
        select.onClick {
            planSelectionListener?.invoke(plan.name, plan.displayName, billableAmount, plan.services, plan.type)
        }
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

    private fun calculatePaidPlanPrice(plan: PlanDetailsItem.PaidPlanDetailsItem, maxPrice: Double) =
        with(binding) {
            val amount =
                when (planDetailsItem) {
                    is PlanDetailsItem.FreePlanDetailsItem -> PRICE_ZERO
                    else -> plan.price.let { price -> cycle.getPrice(price) } ?: PRICE_ZERO
                }.exhaustive

            if (amount != PRICE_ZERO) {
                val discount = (maxPrice - amount) / maxPrice * 100
                planPercentageText.visibility = if (discount > 0) VISIBLE else GONE
                planPercentageText.text = "(-${discount.toInt()}%)"
            }
            val price = amount.formatCentsPriceDefaultLocale(currency.name)
            planPriceText.text = price

            planCycleText.visibility = VISIBLE
            billableAmount = amount
        }
}
