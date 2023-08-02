package me.proton.core.plan.presentation.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import me.proton.core.plan.presentation.databinding.DynamicPlanEntitlementDescriptionViewBinding.inflate
import okhttp3.HttpUrl
import java.io.File
import java.nio.ByteBuffer

class DynamicPlanEntitlementDescriptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding by lazy { inflate(LayoutInflater.from(context), this) }

    private val imageLoader = ImageLoader.Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .build()

    /**
     * The default supported types are:
     *
     * - [String] (treated as a [Uri])
     * - [Uri] (`android.resource`, `content`, `file`, `http`, and `https` schemes)
     * - [HttpUrl]
     * - [File]
     * - [DrawableRes] [Int]
     * - [Drawable]
     * - [Bitmap]
     * - [ByteArray]
     * - [ByteBuffer]
     */
    var icon: Any? = null
        set(value) = with(binding) {
            icon.load(value, imageLoader)
        }

    var text: CharSequence?
        get() = binding.text.text
        set(value) {
            binding.text.text = value
        }
}
