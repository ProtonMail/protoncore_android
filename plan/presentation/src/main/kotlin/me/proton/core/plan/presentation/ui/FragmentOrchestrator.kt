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

package me.proton.core.plan.presentation.ui

import androidx.fragment.app.FragmentManager
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.presentation.utils.inTransaction

private const val TAG_PLANS = "plans_signup_fragment"
private const val TAG_PLANS_UPGRADE = "plans_upgrade_fragment"

fun FragmentManager.showPlansSignup(
    containerId: Int = android.R.id.content,
    planInput: PlanInput
) = findFragmentByTag(TAG_PLANS) ?: run {
    val plansFragment = SignupPlansFragment(planInput)
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, plansFragment, TAG_PLANS)
        addToBackStack(null)
    }
    plansFragment
}

fun FragmentManager.hasPlanSignupFragment() = findFragmentByTag(TAG_PLANS) != null

fun FragmentManager.removePlansSignup() = findFragmentByTag(TAG_PLANS)?.let { plans ->
    inTransaction {
        setCustomAnimations(0, 0)
        remove(plans)
    }
    popBackStack()
}

fun FragmentManager.showPlansForUpgrade(
    containerId: Int = android.R.id.content,
    planInput: PlanInput
) = findFragmentByTag(TAG_PLANS_UPGRADE) ?: run {
    val plansFragment = UpgradePlansFragment(planInput)
    inTransaction {
        setCustomAnimations(0, 0)
        replace(containerId, plansFragment, TAG_PLANS_UPGRADE)
        addToBackStack(null)
    }
    plansFragment
}

fun FragmentManager.removePlansForUpgrade() = findFragmentByTag(TAG_PLANS_UPGRADE)?.let { plans ->
    inTransaction {
        setCustomAnimations(0, 0)
        remove(plans)
    }
    popBackStack()
}
