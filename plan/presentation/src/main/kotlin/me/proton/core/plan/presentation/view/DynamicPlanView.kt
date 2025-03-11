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

package me.proton.core.plan.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import me.proton.core.payment.presentation.view.ProtonPaymentButton
import me.proton.core.plan.presentation.databinding.DynamicPlanViewBinding.inflate
import me.proton.core.presentation.utils.onClick

class DynamicPlanView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding by lazy { inflate(LayoutInflater.from(context), this) }

    init {
        binding.root.onClick {
            isCollapsed = !isCollapsed
        }
    }

    val entitlements: ViewGroup = binding.contentEntitlements

    var title: CharSequence?
        get() = binding.title.text
        set(value) {
            binding.title.text = value
        }

    var description: CharSequence?
        get() = binding.description.text
        set(value) {
            binding.description.text = value
        }

    var promoTitle: CharSequence?
        get() = binding.promoTitle.text
        set(value) {
            binding.promoTitle.text = value
            binding.promoTitle.isVisible = !promoTitle.isNullOrBlank()
        }

    var promoPercentage: CharSequence?
        get() = binding.promoPercentage.text
        set(value) {
            binding.promoPercentage.text = value
            binding.promoPercentage.isVisible = !promoPercentage.isNullOrBlank()
        }

    var starred: Boolean
        get() = binding.starred.isVisible
        set(value) {
            binding.starred.isVisible = value
        }

    var priceText: CharSequence?
        get() = binding.priceText.text
        set(value) {
            binding.priceText.text = value
            binding.priceLayout.isVisible = !priceText.isNullOrBlank()
        }

    var priceCycle: CharSequence?
        get() = binding.priceCycle.text
        set(value) {
            binding.priceCycle.text = value
            binding.priceCycle.isVisible = !priceCycle.isNullOrBlank()
        }

    var pricePercentage: CharSequence?
        get() = binding.pricePercentage.text
        set(value) {
            binding.pricePercentage.text = value
            binding.pricePercentage.isVisible = !pricePercentage.isNullOrBlank()
            binding.priceLayout.isVisible = !priceText.isNullOrBlank()
        }

    var isCollapsable: Boolean
        get() = binding.collapse.isVisible
        set(value) {
            binding.collapse.isVisible = value
            binding.content.isVisible = !value
        }

    var isCollapsed: Boolean
        get() = !binding.content.isVisible
        set(value) {
            if (!isCollapsable) return
            if (isCollapsed == value) return
            when (value) {
                true -> {
                    binding.content.isVisible = false
                    binding.collapse.rotate(-180f)
                }

                false -> {
                    binding.content.isVisible = true
                    binding.collapse.rotate(+180f)
                }
            }
        }

    var autoRenewalText: CharSequence?
        get() = binding.autoRenewInfo.text
        set(value) {
            binding.autoRenewInfo.text = value
            binding.autoRenewInfo.isVisible = !autoRenewalText.isNullOrBlank()
        }

    var renewalText: CharSequence?
        get() = binding.contentRenewal.text
        set(value) {
            binding.contentRenewal.text = value
            binding.contentRenewal.isVisible = !renewalText.isNullOrBlank()
            binding.contentSeparator.isVisible = !renewalText.isNullOrBlank()
        }

    var contentButtonIsEnabled: Boolean
        get() = binding.contentButton.isEnabled
        set(value) {
            binding.contentButton.isEnabled = value
        }

    var contentButtonIsVisible: Boolean
        get() = binding.contentButton.isVisible
        set(value) {
            binding.contentButton.isVisible = value
        }

    var contentButtonText: CharSequence?
        get() = binding.contentButton.text
        set(value) {
            binding.contentButton.text = value
        }

    var paymentButtonIsVisible: Boolean
        get() = binding.paymentButtonStub.isVisible
        set(value) {
            binding.paymentButtonStub.isVisible = value
        }

    /** Inflates the [ProtonPaymentButton]. Make sure to pass a unique [id]. */
    fun inflatePaymentButton(@IdRes id: Int): ProtonPaymentButton {
        binding.paymentButtonStub.inflatedId = id
        return binding.paymentButtonStub.inflate() as ProtonPaymentButton
    }

    fun setOnButtonClickListener(listener: OnClickListener) {
        binding.contentButton.setOnClickListener(listener)
    }
}

private fun View.rotate(degrees: Float) {
    animate().rotation(rotation + degrees).interpolator = AccelerateDecelerateInterpolator()
}
