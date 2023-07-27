package me.proton.core.plan.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.DynamicPlanEntitlementStorageViewBinding.inflate

class DynamicPlanEntitlementStorageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding by lazy { inflate(LayoutInflater.from(context), this) }

    var text: String?
        get() = binding.text.text.toString()
        set(value) {
            binding.text.text = value
        }

    var progress: Int
        get() = binding.progress.progress
        set(value) {
            binding.progress.progress = value
            val indicatorColor = when {
                value < 50 -> ContextCompat.getColor(context, R.color.notification_success)
                value < 90 -> ContextCompat.getColor(context, R.color.notification_warning)
                else -> ContextCompat.getColor(context, R.color.notification_error)
            }
            binding.progress.setIndicatorColor(indicatorColor)
        }
}
