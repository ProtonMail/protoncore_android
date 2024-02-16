/*
 * Copyright (c) 2024 Proton Technologies AG
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
import android.widget.FrameLayout
import me.proton.core.domain.entity.Product
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.StorageNearlyFullBinding.inflate

class StorageNearlyFullCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = inflate(LayoutInflater.from(context), this)

    fun setStorageFull(product: Product) {
        when (product) {
            Product.Drive -> R.string.plans_subscription_drive_storage_full_title
            Product.Mail -> R.string.plans_subscription_base_storage_full_title
            else -> null
        }?.let {
            resources.getString(it)
        }.let {
            binding.titleView.text = it
        }
    }

    fun setStorageNearlyFull(product: Product) {
        when (product) {
            Product.Drive -> R.string.plans_subscription_drive_storage_nearly_full_title
            Product.Mail -> R.string.plans_subscription_base_storage_nearly_full_title
            else -> null
        }?.let {
            resources.getString(it)
        }.let {
            binding.titleView.text = it
        }
    }

    fun onUpgradeAvailable(listener: () -> Unit) {
        binding.actionButton.setOnClickListener { listener() }
    }

    fun onUpgradeUnavailable() {
        binding.actionButton.text = context.getString(R.string.plans_can_not_upgrade_from_mobile)
    }
}
