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
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.entity.PlanDetailsItem
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
    plan: PlanDetailsItem
): Pair<String, Int> {
    if (type.contains(KEY_FEATURE_STORAGE)) {
        val quantity = plan.storage.formatByteToHumanReadable()
        val value = context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), 0)
        return Pair(value.replace(KEY_FEATURE_STORAGE, quantity), R.drawable.ic_baseline_check)
    }
    if (type.contains(KEY_FEATURE_ADDRESSES)) {
        val quantity = plan.addresses
        val value =
            context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity)
        return Pair(value.replace(KEY_FEATURE_ADDRESSES, quantity.toString()), R.drawable.ic_baseline_check)
    }
    if (type.contains(KEY_FEATURE_VPN)) {
        val quantity = plan.connections
        val value =
            context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity)
        return Pair(value.replace(KEY_FEATURE_VPN, quantity.toString()), R.drawable.ic_baseline_check)
    }
    if (type.contains(KEY_FEATURE_DOMAINS)) {
        val quantity = plan.domains
        val value = context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), plan.domains)
        return Pair(value.replace(KEY_FEATURE_DOMAINS, quantity.toString()), R.drawable.ic_baseline_check)
    }
    if (type.contains(KEY_FEATURE_USERS)) {
        val quantity = plan.members
        val value = context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity)
        return Pair(value.replace(KEY_FEATURE_USERS, quantity.toString()), R.drawable.ic_baseline_check)
    }
    if (type.contains(KEY_FEATURE_CALENDARS)) {
        val quantity = plan.calendars
        val value =
            context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity)
        return Pair(value.replace(KEY_FEATURE_CALENDARS, quantity.toString()), R.drawable.ic_baseline_check)
    }
    return Pair(context.getString(resourceValuesArray.getResourceId(index, 0)), R.drawable.ic_baseline_check)
}

internal fun createCurrentPlanFeature(
    type: String,
    resourceValuesArray: TypedArray,
    index: Int,
    context: Context,
    plan: PlanDetailsItem.CurrentPlanDetailsItem
): Pair<String, Int> {
    if (type.contains(KEY_FEATURE_ADDRESSES)) {
        val quantity = plan.addresses
        val value = String.format(
            context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity),
            plan.usedAddresses
        )
        return Pair(value.replace(KEY_FEATURE_ADDRESSES, quantity.toString()), R.drawable.ic_envelope)
    }
    if (type.contains(KEY_FEATURE_VPN)) {
        val quantity = plan.connections
        val value =
            context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity)
        return Pair(value.replace(KEY_FEATURE_VPN, quantity.toString()), R.drawable.ic_shield)
    }
    if (type.contains(KEY_FEATURE_DOMAINS)) {
        val quantity = plan.domains
        val value = String.format(
            context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), plan.domains),
            plan.usedDomains
        )
        return Pair(value.replace(KEY_FEATURE_DOMAINS, quantity.toString()), R.drawable.ic_globe_language)
    }
    if (type.contains(KEY_FEATURE_USERS)) {
        val quantity = plan.members
        val value = String.format(
            context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity),
            plan.usedMembers
        )
        return Pair(value.replace(KEY_FEATURE_USERS, quantity.toString()), R.drawable.ic_user)
    }
    if (type.contains(KEY_FEATURE_CALENDARS)) {
        val quantity = plan.calendars
        val value = String.format(
            context.resources.getQuantityString(resourceValuesArray.getResourceId(index, 0), quantity),
            plan.usedCalendars
        )
        return Pair(value.replace(KEY_FEATURE_CALENDARS, quantity.toString()), R.drawable.ic_calendar_checkmark)
    }
    return Pair(context.getString(resourceValuesArray.getResourceId(index, 0)), R.drawable.ic_baseline_check)
}
