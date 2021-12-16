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
import android.content.res.TypedArray
import me.proton.core.plan.presentation.entity.PlanDetailsListItem
import me.proton.core.presentation.utils.formatByteToHumanReadable

private const val KEY_FEATURE_VPN = "#proton_vpn#"
private const val KEY_FEATURE_STORAGE = "#proton_storage#"
private const val KEY_FEATURE_ADDRESSES = "#proton_addresses#"
private const val KEY_FEATURE_DOMAINS = "#proton_domains#"
private const val KEY_FEATURE_USERS = "#proton_users#"
private const val KEY_FEATURE_CALENDARS = "#proton_calendars#"

internal fun createPlanFeature(
    type: String,
    resourceValuesArray: TypedArray,
    index: Int,
    context: Context,
    plan: PlanDetailsListItem.PaidPlanDetailsListItem
): PlanContentItemView {
    if (type.contains(KEY_FEATURE_STORAGE)) {
        return PlanContentItemView(context).apply {
            val quantity = plan.storage.formatByteToHumanReadable()
            val value = context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), 0)
            planItem = value.replace(KEY_FEATURE_STORAGE, quantity)
        }
    }
    if (type.contains(KEY_FEATURE_ADDRESSES)) {
        return PlanContentItemView(context).apply {
            val quantity = plan.addresses
            val value =
                context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity)
            planItem = value.replace(KEY_FEATURE_ADDRESSES, quantity.toString())
        }
    }
    if (type.contains(KEY_FEATURE_VPN)) {
        return PlanContentItemView(context).apply {
            val quantity = plan.connections
            val value =
                context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity)
            planItem = value.replace(KEY_FEATURE_VPN, quantity.toString())
        }
    }
    if (type.contains(KEY_FEATURE_DOMAINS)) {
        return PlanContentItemView(context).apply {
            val quantity = plan.domains
            val value = context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), plan.domains)
            planItem = value.replace(KEY_FEATURE_DOMAINS, quantity.toString())
        }
    }
    if (type.contains(KEY_FEATURE_USERS)) {
        return PlanContentItemView(context).apply {
            val quantity = plan.members
            val value = context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity)
            planItem = value.replace(KEY_FEATURE_USERS, quantity.toString())
        }
    }
    if (type.contains(KEY_FEATURE_CALENDARS)) {
        return PlanContentItemView(context).apply {
            val quantity = plan.calendars
            val value =
                context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity)
            planItem = value.replace(KEY_FEATURE_CALENDARS, quantity.toString())
        }
    }
    return PlanContentItemView(context).apply {
        planItem = context.getString(resourceValuesArray.getResourceId(index, 0))
    }
}
