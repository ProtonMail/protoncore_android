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

package me.proton.core.plan.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.proton.core.payment.presentation.view.ProtonPaymentEventListener
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentEvent
import me.proton.core.plan.domain.IsDynamicPlanAdjustedPriceEnabled
import me.proton.core.plan.domain.entity.DynamicDecoration
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.firstSubtitleOrNull
import me.proton.core.plan.domain.entity.firstTitleOrNull
import me.proton.core.plan.domain.entity.isFree
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentDynamicPlanListBinding
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.entity.getSelectedPlan
import me.proton.core.plan.presentation.view.DynamicPlanCardView
import me.proton.core.plan.presentation.view.DynamicPlanView
import me.proton.core.plan.presentation.view.toView
import me.proton.core.plan.presentation.viewmodel.DynamicPlanListViewModel
import me.proton.core.plan.presentation.viewmodel.DynamicPlanListViewModel.Action
import me.proton.core.plan.presentation.viewmodel.DynamicPlanListViewModel.State
import me.proton.core.plan.presentation.entity.DynamicUser
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.viewBinding
import java.util.Objects
import javax.inject.Inject
import kotlin.math.abs

@Suppress("TooManyFunctions")
@AndroidEntryPoint
class DynamicPlanListFragment : ProtonFragment(R.layout.fragment_dynamic_plan_list) {
    @Inject
    internal lateinit var isDynamicPlanAdjustedPriceEnabled: IsDynamicPlanAdjustedPriceEnabled

    private val binding by viewBinding(FragmentDynamicPlanListBinding::bind)
    private val viewModel by viewModels<DynamicPlanListViewModel>()

    private var onPlanSelected: ((SelectedPlan) -> Unit)? = null
    private var onProtonPaymentEventListener: ProtonPaymentEventListener? = null

    fun onError(): Flow<Throwable?> = viewModel.state.map { (it as? State.Error)?.error }

    fun getUser(): StateFlow<DynamicUser> = viewModel.getUser()
    fun getPlanList(): StateFlow<List<DynamicPlan>?> = viewModel.getPlanList()

    fun setOnPlanSelected(onPlanSelected: (SelectedPlan) -> Unit) {
        this.onPlanSelected = onPlanSelected
    }

    fun setOnProtonPaymentResultListener(listener: ProtonPaymentEventListener) {
        this.onProtonPaymentEventListener = listener
    }

    fun setUser(user: DynamicUser) {
        viewModel.perform(Action.SetUser(user))
    }

    fun setCycle(cycle: Int) {
        viewModel.perform(Action.SetCycle(cycle))
    }

    fun setCurrency(currency: String) {
        viewModel.perform(Action.SetCurrency(currency))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.onEach {
            when (it) {
                is State.Loading -> onLoading()
                is State.Error -> onError(it.error)
                is State.Success -> onSuccess(it)
            }
        }.launchInViewLifecycleScope()

        binding.retry.onClick { viewModel.perform(Action.Load) }
    }

    private fun onLoading() {
        showLoading(true)
    }

    private fun onError(error: Throwable?) = with(binding) {
        showLoading(false)
        val message = error?.getUserMessage(resources)
        showError(message ?: getString(R.string.presentation_error_general))
    }

    private fun onSuccess(result: State.Success) {
        showLoading(false)
        showPlans(result.plans, result.filter.cycle, result.filter.currency)
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        progress.isVisible = loading
        errorLayout.isVisible = false
        binding.plans.removeAllViews()
    }

    private fun showError(message: String) = with(binding) {
        errorLayout.isVisible = true
        error.text = message
    }

    private fun showPlans(plans: List<DynamicPlan>, cycle: Int, currency: String) {
        binding.plans.removeAllViews()
        plans.forEach { plan ->
            val cardView = DynamicPlanCardView(requireContext())
            val selectedPlan = plan.getSelectedPlan(resources, cycle, currency)
            cardView.planView.setPlan(plan, cycle, currency)
            cardView.planView.setOnButtonClickListener { onPlanSelected?.invoke(selectedPlan) }
            binding.plans.addView(cardView)
        }
    }

    private fun DynamicPlanView.setPlan(plan: DynamicPlan, cycle: Int, currency: String) {
        val instance = plan.instances[cycle]
        val price = instance?.price?.get(currency)
        val priceCurrent = price?.current ?: 0.0
        val priceCurrency = price?.currency ?: currency
        val badges = plan.decorations.filterIsInstance<DynamicDecoration.Badge>()
        val promoTitleBadge = badges.firstTitleOrNull(instance?.price?.get(currency)?.id)
        val promoSubtitleBadge = badges.firstSubtitleOrNull(instance?.price?.get(currency)?.id)
        val stars = plan.decorations.filterIsInstance<DynamicDecoration.Starred>()
        val userId = getUser().value.userId

        id = abs(plan.name.hashCode())
        title = plan.title
        description = plan.description
        starred = stars.isNotEmpty()
        promoPercentage = promoTitleBadge?.text
        promoTitle = promoSubtitleBadge?.text
        priceText = priceCurrent.toDouble().formatCentsPriceDefaultLocale(priceCurrency)
        priceCycle = instance?.description
        isCollapsable = true
        entitlements.removeAllViews()
        plan.entitlements.forEach { entitlements.addView(it.toView(context)) }
        contentButtonIsVisible = true
        contentButtonText = String.format(context.getString(R.string.plans_get_proton), plan.title)

        if (isDynamicPlanAdjustedPriceEnabled(userId)) {
            contentButtonIsVisible = plan.isFree()

            val paymentButton = inflatePaymentButton(id = Objects.hash(plan.name, currency, cycle))
            paymentButton.isVisible = !plan.isFree()
            paymentButton.currency = currency
            paymentButton.cycle = cycle
            paymentButton.plan = plan
            paymentButton.paymentProvider = null // determined automatically
            paymentButton.userId = userId
            paymentButton.setOnEventListener(this@DynamicPlanListFragment::onProtonPaymentEvent)
        }
    }

    private fun onProtonPaymentEvent(event: ProtonPaymentEvent) {
        val isContentButtonEnabled = event !is ProtonPaymentEvent.Loading
        binding.plans.forEach {
            (it as? DynamicPlanCardView)?.planView?.contentButtonIsEnabled = isContentButtonEnabled
        }
        onProtonPaymentEventListener?.invoke(event)
    }
}
