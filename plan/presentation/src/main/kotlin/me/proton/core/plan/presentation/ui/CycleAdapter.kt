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

import android.content.Context
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import me.proton.core.plan.presentation.R

class CycleAdapter(
    context: Context,
    @LayoutRes layout: Int
) : ArrayAdapter<String>(context, layout) {

    private val cycleIndexed = mutableListOf<Int>()

    override fun clear() {
        super.clear()
        cycleIndexed.clear()
    }

    fun addAll(cycles: List<Int>) {
        cycles.forEach {
            cycleIndexed.add(it)
            add(getString(it))
        }
    }

    fun getCycle(index: Int): Int = cycleIndexed[index]

    @Suppress("MagicNumber")
    fun getString(cycle: Int) = when (cycle) {
        1 -> context.getString(R.string.plans_pay_monthly)
        12 -> context.getString(R.string.plans_pay_annually)
        24 -> context.getString(R.string.plans_pay_biennially)
        else -> context.resources.getQuantityString(R.plurals.plans_pay_other, cycle, cycle)
    }
}
