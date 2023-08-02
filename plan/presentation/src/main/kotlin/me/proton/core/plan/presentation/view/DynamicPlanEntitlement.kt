/*
 * Copyright (c) 2023 Proton Technologies AG
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

@file:Suppress("MagicNumber")

package me.proton.core.plan.presentation.view

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import me.proton.core.plan.domain.entity.DynamicPlanEntitlement
import me.proton.core.plan.presentation.R
import me.proton.core.util.kotlin.takeIfNotBlank

fun DynamicPlanEntitlement.toView(context: Context) = when (this) {
    is DynamicPlanEntitlement.Description -> DynamicPlanEntitlementDescriptionView(context).apply {
        icon = this@toView.iconBase64.takeIfNotBlank()?.toByteArray() ?: getFallbackIcon(context)
        text = this@toView.text
    }

    is DynamicPlanEntitlement.Storage -> DynamicPlanEntitlementStorageView(context).apply {
        text = formatUsedSpace(context, currentMBytes / 100, maxMBytes / 100)
        progress = ((currentMBytes.toFloat() / maxMBytes.toFloat()) * 100).toInt()
    }
}

private fun getFallbackIcon(context: Context) =
    ResourcesCompat.getDrawable(context.resources, R.drawable.ic_proton_checkmark, null)
