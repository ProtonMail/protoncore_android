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
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.entity.PlanDetailsListItem
import me.proton.core.presentation.utils.formatByteToHumanReadable

private const val KEY_FEATURE_VPN = "#proton_vpn#"
private const val KEY_FEATURE_STORAGE = "#proton_storage#"
private const val KEY_FEATURE_ADDRESSES = "#proton_addresses#"
private const val KEY_FEATURE_DOMAINS = "#proton_domains#"
private const val KEY_FEATURE_USERS = "#proton_users#"
private const val KEY_FEATURE_CALENDARS = "#proton_calendars#"

internal fun String.createPlanFeature(
    context: Context,
    plan: PlanDetailsListItem.PaidPlanDetailsListItem
): PlanContentItemView {
    if (contains(KEY_FEATURE_STORAGE)) {
        return PlanContentItemView(context).apply {
            planItem =
                if (plan.storage > 0) {
                    this@createPlanFeature.replace(KEY_FEATURE_STORAGE, plan.storage.formatByteToHumanReadable())
                } else context.getString(R.string.plans_feature_free_item_storage)
        }
    }
    if (contains(KEY_FEATURE_ADDRESSES)) {
        return PlanContentItemView(context).apply {
            planItem =
                if (plan.addresses > 0) {
                    this@createPlanFeature.replace(KEY_FEATURE_ADDRESSES, plan.addresses.toString())
                } else context.getString(R.string.plans_feature_free_item_address)
        }
    }
    if (contains(KEY_FEATURE_VPN)) {
        return PlanContentItemView(context).apply {
            planItem =
                if (plan.connections > 0) {
                    this@createPlanFeature.replace(KEY_FEATURE_VPN, plan.connections.toString())
                } else context.getString(R.string.plans_feature_free_item_vpn)
        }
    }
    if (contains(KEY_FEATURE_DOMAINS)) {
        return PlanContentItemView(context).apply {
            planItem = this@createPlanFeature.replace(KEY_FEATURE_DOMAINS, plan.domains.toString())
        }
    }
    if (contains(KEY_FEATURE_USERS)) {
        return PlanContentItemView(context).apply {
            planItem = this@createPlanFeature.replace(KEY_FEATURE_USERS, plan.members.toString())
        }
    }
    if (contains(KEY_FEATURE_CALENDARS)) {
        return PlanContentItemView(context).apply {
            planItem =
                if (plan.calendars > 0) {
                    this@createPlanFeature.replace(KEY_FEATURE_CALENDARS, plan.calendars.toString())
                } else context.getString(R.string.plans_feature_free_item_calendar)
        }
    }
    return PlanContentItemView(context).apply {
        planItem = this@createPlanFeature
    }
}
