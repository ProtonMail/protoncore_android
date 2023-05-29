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
import android.content.res.Resources.NotFoundException
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import me.proton.core.plan.domain.entity.PLAN_PRODUCT
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.PlanItemBinding
import me.proton.core.plan.presentation.entity.PlanCurrency
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import me.proton.core.plan.presentation.entity.SubscribedPlan
import me.proton.core.presentation.utils.PRICE_ZERO
import me.proton.core.presentation.utils.Price
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.CoreLogger
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

    // For all plan ids.
    private val mappedPlanIds by lazy { context.getStringArrayByName(R.array.plan_mapping_plan_ids) }

    // There are 3 type of layouts: current, free and paid.
    private val mappedCurrentPlanLayouts by lazy {
        context.getStringArrayByName(R.array.plan_mapping_current_plan_layouts)
    }
    private val mappedFreePlanLayouts by lazy { context.getStringArrayByName(R.array.plan_mapping_free_plan_layouts) }
    private val mappedPaidPlanLayouts by lazy { context.getStringArrayByName(R.array.plan_mapping_paid_plan_layouts) }

    private fun getMappedLayout(plan: PlanDetailsItem, mappedPlanLayouts: Array<String>?): String {
        val indexOfPlanName = mappedPlanIds?.indexOf(plan.name) ?: return "plan_id_${plan.name}"
        return mappedPlanLayouts?.getOrNull(indexOfPlanName) ?: "plan_id_${plan.name}"
    }

    private fun getCurrentLayout(plan: PlanDetailsItem) = getMappedLayout(plan, mappedCurrentPlanLayouts)

    private fun getFreeLayout(plan: PlanDetailsItem) = getMappedLayout(plan, mappedFreePlanLayouts)

    private fun getPaidLayout(plan: PlanDetailsItem) = getMappedLayout(plan, mappedPaidPlanLayouts)

    /**
     * Sets plans data and binds to according plan item UIs.
     * Returns a result of the operation boolean true or false.
     */
    fun setData(
        subscribedPlan: SubscribedPlan
    ): Boolean {
        this.planDetailsItem = subscribedPlan.plan
        this.cycle = subscribedPlan.cycle
        this.currency = subscribedPlan.currency ?: PlanCurrency.EUR
        this.collapsible = subscribedPlan.collapsible

        initCommonViews(subscribedPlan.plan)
        return when (subscribedPlan.plan) {
            is PlanDetailsItem.FreePlanDetailsItem -> bindFreePlan(subscribedPlan.plan)
            is PlanDetailsItem.PaidPlanDetailsItem -> bindPaidPlan(subscribedPlan.plan)
            is PlanDetailsItem.CurrentPlanDetailsItem -> bindCurrentPlan(
                subscribedPlan.amount?.toDouble(),
                subscribedPlan.storageBar,
                subscribedPlan.plan
            )
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

    /**
     * Binds the current plan to the plan item UI. Returns a result of the operation boolean true or false.
     */
    private fun bindCurrentPlan(
        amount: Price?,
        storageBar: Boolean,
        plan: PlanDetailsItem.CurrentPlanDetailsItem
    ): Boolean = with(binding) {
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
        storageText.isVisible = storageBar
        storageProgress.isVisible = storageBar

        planDescriptionText.text = context.getString(R.string.plans_current_plan)
        planPercentageText.visibility = GONE
        select.visibility = GONE
        val mappedLayout = getCurrentLayout(plan)
        val featureOrder = context.getStringArrayByName("${mappedLayout}_order")
        val featureIcons = context.getIntegerArrayByName("${mappedLayout}_icons")
        if (featureOrder == null || featureIcons == null) {
            featureIcons?.recycle()
            return false
        }
        context.getIntegerArrayByName(mappedLayout)?.let {
            bindPlanFeatures(
                length = it.length().minus(1)
            ) { index: Int ->
                createCurrentPlanFeature(
                    featureOrder[index - 1],
                    featureIcons.getResourceId(index - 1, 0),
                    it,
                    index,
                    context,
                    plan
                )
            }
            featureIcons.recycle()
            it.recycle()
        }

        when (plan.cycle) {
            PlanCycle.MONTHLY -> planCycleText.text = context.getString(R.string.plans_billing_monthly)
            PlanCycle.YEARLY -> planCycleText.text = context.getString(R.string.plans_billing_yearly)
            PlanCycle.TWO_YEARS -> planCycleText.text = context.getString(R.string.plans_billing_two_years)
            null, PlanCycle.FREE -> planCycleText.visibility = GONE
            PlanCycle.OTHER -> {
                val duration = plan.cycle.cycleDurationMonths
                planCycleText.text =
                    String.format(
                        context.resources.getQuantityString(
                            R.plurals.plans_billing_other_period,
                            duration
                        ), duration
                    )
            }
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

        val amount = amount ?: plan.price?.let { price -> plan.cycle?.getPrice(price) } ?: PRICE_ZERO
        billableAmount = amount
        planPriceText.text = amount.formatCentsPriceDefaultLocale(currency.name)
        return true
    }

    /**
     * Binds a free plan to the plan item UI. Returns a result of the operation boolean true or false.
     */
    private fun bindFreePlan(plan: PlanDetailsItem.FreePlanDetailsItem): Boolean = with(binding) {
        select.text = context.getString(R.string.plans_proton_for_free)
        planCycleText.visibility = View.GONE
        planPriceText.text = PRICE_ZERO.formatCentsPriceDefaultLocale(currency.name)

        val mappedLayout = getFreeLayout(plan)
        val featureOrder = context.getStringArrayByName("${mappedLayout}_order")
        val featureIcons = context.getIntegerArrayByName("${mappedLayout}_icons")
        if (featureOrder == null || featureIcons == null) {
            featureIcons?.recycle()
            return false
        }
        context.getIntegerArrayByName(mappedLayout)?.let {
            bindPlanFeatures(
                length = it.length().minus(1)
            ) { index: Int ->
                createPlanFeature(
                    featureOrder[index - 1],
                    featureIcons.getResourceId(index - 1, 0),
                    it,
                    index,
                    context,
                    plan
                )
            }
            binding.planDescriptionText.text = context.getString(it.getResourceId(0, 0))
            featureIcons.recycle()
            it.recycle()
        }
        select.onClick {
            planSelectionListener?.invoke(plan.name, plan.displayName, billableAmount, 0, PLAN_PRODUCT)
        }
        return true
    }

    /**
     * Binds a paid plan to the plan item UI. Returns a result of the operation boolean true or false.
     */
    private fun bindPaidPlan(plan: PlanDetailsItem.PaidPlanDetailsItem): Boolean = with(binding) {
        select.text = String.format(context.getString(R.string.plans_get_proton), plan.displayName)
        starred.visibility = if (plan.starred) VISIBLE else INVISIBLE
        val mappedLayout = getPaidLayout(plan)
        val featureOrder = context.getStringArrayByName("${mappedLayout}_order")
        val featureIcons = context.getIntegerArrayByName("${mappedLayout}_icons")
        if (featureOrder == null || featureIcons == null) {
            featureIcons?.recycle()
            return false
        }
        context.getIntegerArrayByName(mappedLayout)?.let {
            bindPlanFeatures(
                length = it.length().minus(1)
            ) { index: Int ->
                createPlanFeature(
                    featureOrder[index - 1],
                    featureIcons.getResourceId(index - 1, 0),
                    it,
                    index,
                    context,
                    plan
                )
            }
            binding.planDescriptionText.text = context.getString(it.getResourceId(0, 0))
            it.recycle()
            featureIcons.recycle()
        }

        val maxPrice = cycle.getPrice(plan.price) ?: PRICE_ZERO
        calculatePaidPlanPrice(plan = plan, maxPrice = maxPrice)
        handlePromotion(plan)
        if (!plan.purchaseEnabled) {
            select.visibility = GONE
            priceCycleLayout.visibility = GONE
        }
        select.onClick {
            planSelectionListener?.invoke(plan.name, plan.displayName, billableAmount, plan.services, plan.type)
        }
        return true
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

    private fun handlePromotion(plan: PlanDetailsItem.PaidPlanDetailsItem) = with(binding) {
        val promoPercentage = cycle.promotionPercentage(plan.promotionPercentage)
        val promotionOngoing = if (promoPercentage > 0) VISIBLE else GONE
        planPromoPercentage.text = "-${promoPercentage}%"
        planPromoPercentage.visibility = promotionOngoing
        planPromoTitle.visibility = promotionOngoing
    }
}
