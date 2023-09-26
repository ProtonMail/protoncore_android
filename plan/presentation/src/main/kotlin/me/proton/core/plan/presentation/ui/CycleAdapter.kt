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
        else -> context.getString(R.string.plans_pay_other, cycle)
    }
}
