/*
 * Copyright (c) 2022 Proton Technologies AG
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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.text.Spanned
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ArrayRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import me.proton.core.plan.presentation.R
import me.proton.core.presentation.utils.formatByteToHumanReadable
import me.proton.core.user.domain.entity.User
import me.proton.core.util.kotlin.CoreLogger
import java.text.DateFormat
import java.time.Instant
import java.util.Calendar

const val HUNDRED_PERCENT = 100

/** The min percentage of used storage, when we start showing a warning message. */
internal const val STORAGE_WARNING_THRESHOLD = 50

/** The min percentage of used storage, when we start showing an error message. */
const val STORAGE_ERROR_THRESHOLD = 80

object LogTag {
    const val PLAN_RESOURCE_ERROR = "core.plan.presentation.resource"
}

internal fun Context.getStringArrayByName(aString: String): Array<out String>? {
    val result = getStringArrayByName(resources.getIdentifier(aString, "array", packageName))
    if (result == null) {
        CoreLogger.e(
            LogTag.PLAN_RESOURCE_ERROR,
            Resources.NotFoundException("Plan config resource not found $aString"),
            "Plan config resource not found $aString"
        )
    }
    return result
}

internal fun Context.getStringArrayByName(@ArrayRes res: Int) =
    try {
        resources.getStringArray(res)
    } catch (notFound: Resources.NotFoundException) {
        CoreLogger.e(LogTag.PLAN_RESOURCE_ERROR, notFound)
        null
    }

internal fun Context.getIntegerArrayByName(aString: String): TypedArray? {
    val result = getIntegerArrayByName(resources.getIdentifier(aString, "array", packageName))
    if (result == null) {
        CoreLogger.e(
            LogTag.PLAN_RESOURCE_ERROR,
            Resources.NotFoundException("Plan config resource not found $aString"),
            "Plan config resource not found $aString"
        )
    }
    return result
}

@SuppressLint("Recycle")
internal fun Context.getIntegerArrayByName(@ArrayRes aString: Int) =
    try {
        resources.obtainTypedArray(aString)
    } catch (notFound: Resources.NotFoundException) {
        CoreLogger.e(LogTag.PLAN_RESOURCE_ERROR, notFound)
        null
    }

internal fun User?.calculateUsedSpacePercentage(): Double {
    if (this == null) return 0.0
    return usedSpace.toDouble() / maxSpace.toDouble() * HUNDRED_PERCENT
}

internal fun formatUsedSpace(context: Context, usedBytes: Long, maxBytes: Long): String = String.format(
    context.getString(R.string.plans_used_space),
    usedBytes.formatByteToHumanReadable(),
    maxBytes.formatByteToHumanReadable()
)

internal fun formatRenew(context: Context, renew: Boolean, periodEnd: Instant): Spanned {
    val date = Calendar.getInstance().apply { timeInMillis = periodEnd.toEpochMilli() }.time
    val renewalText = if (renew) R.string.plans_renewal_date else R.string.plans_expiration_date
    return HtmlCompat.fromHtml(
        String.format(
            context.getString(renewalText),
            DateFormat.getDateInstance().format(date)
        ),
        HtmlCompat.FROM_HTML_MODE_LEGACY
    )
}

internal fun PlanItemView.rotate() = with(binding) {
    if (!collapsible) {
        planGroup.visibility = ConstraintLayout.VISIBLE
        return@with
    }
    val degrees = if (planGroup.visibility == ConstraintLayout.VISIBLE) {
        planGroup.visibility = ConstraintLayout.GONE
        (root.parent as PlanItemView).isSelected = false
        setBackgroundResource(R.drawable.background_plan_list_item)
        360f
    } else {
        planGroup.visibility = ConstraintLayout.VISIBLE
        (root.parent as PlanItemView).isSelected = true
        setBackgroundResource(R.drawable.background_plan_list_item_opened)
        180f
    }
    collapse.animate().rotation(degrees).interpolator = AccelerateDecelerateInterpolator()
}
