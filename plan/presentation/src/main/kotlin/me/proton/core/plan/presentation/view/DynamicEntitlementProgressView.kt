package me.proton.core.plan.presentation.view

import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getColorStateList
import androidx.core.content.ContextCompat.getDrawable
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.DynamicEntitlementStorageViewBinding.inflate

@Suppress("MagicNumber")
class DynamicEntitlementProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding by lazy { inflate(LayoutInflater.from(context), this) }

    var tagText: CharSequence?
        get() = binding.tagText.text
        set(value) {
            binding.tagText.text = value
        }

    var text: CharSequence?
        get() = binding.text.text
        set(value) {
            binding.text.text = value
        }

    var progress: Int
        get() = binding.progress.progress
        private set(value) {
            binding.progress.progress = value
            updateProgressIndicatorColor()
            updateTextColors()
        }

    var progressMin: Int
        get() = if (Build.VERSION.SDK_INT >= VERSION_CODES.O) { binding.progress.min } else { 0 }
        private set(value) {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.O) { binding.progress.min = value }
        }

    var progressMax: Int
        get() = binding.progress.max
        private set(value) {
            binding.progress.max = value
        }

    private val percentage: Double
        get() = progress.toDouble() / (progressMax - progressMin) * 100.0

    /** Set values for min, max and current progress.
     * The setters are not exposed individually, to prevent potential ordering issues.
     * For example, calling `progress = X`, then `progressMax = Y` may have a different effect,
     * compared to calling `progressMax = Y` first, and then `progress = X`.
     */
    fun setProgress(min: Int = 0, max: Int = 100, current: Int) {
        progressMin = min
        progressMax = max
        progress = current
    }

    private fun updateProgressIndicatorColor() {
        val indicatorColor = when {
            percentage < STORAGE_WARNING_THRESHOLD ->
                getColor(context, R.color.notification_success)

            percentage < STORAGE_ERROR_THRESHOLD ->
                getColor(context, R.color.notification_warning)

            else ->
                getColor(context, R.color.notification_error)
        }
        binding.progress.setIndicatorColor(indicatorColor)
    }

    private fun updateTextColors() {
        if (percentage >= STORAGE_ERROR_THRESHOLD) {
            val errorColor = getColor(context, R.color.notification_error)
            val exclamation =
                getDrawable(context, R.drawable.ic_proton_exclamation_circle_filled)
            exclamation?.setTint(errorColor)
            binding.tagText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                exclamation,
                null
            )
            binding.text.setTextColor(errorColor)
        } else {
            binding.tagText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            binding.text.setTextColor(getColorStateList(context, R.color.text_norm_selector))
        }
    }
}
