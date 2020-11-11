package me.proton.android.core.presentation.ui.view

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import me.proton.android.core.presentation.R
import me.proton.android.core.presentation.ui.view.ProtonProgressButton.State
import me.proton.android.core.presentation.ui.view.ProtonProgressButton.State.IDLE
import me.proton.android.core.presentation.ui.view.ProtonProgressButton.State.LOADING

/**
 * Custom Proton button that includes a loading spinner (indefinite progress).
 * Supports normal button mode (default) or text-only mode by supplying the  appropriate style.
 *
 * The styles are default `@style/ProtonButton` (which is a default button style in `ProtonTheme` and is not
 * mandatory to be set.
 * The other style is `@style/ProtonButton.Borderless.Text` for the text-only button.
 * This one needs to be set in the xml.
 *
 * There are 2 [State]s supported ([IDLE] and [LOADING]), and the they can be controlled with the public functions
 * exposed.
 *
 * Note: ProgressButton includes a spinner (indefinite progress) which can be turned on or off with the [setLoading]
 * and [loadingComplete] functions. [loadingComplete] is part of [Loadable] interface, for more convenient use.
 *
 * @author Dino Kadrikj.
 */
open class ProtonProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : ProtonButton(context, attrs, defStyleAttr), Loadable {

    var currentState: State = IDLE
        private set

    var autoLoading: Boolean = false

    @DrawableRes
    private var progressDrawableId: Int = R.drawable.ic_animated_loading
    private val progressDrawable by lazy {
        ContextCompat.getDrawable(context, progressDrawableId) as AnimatedVectorDrawable
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.ProtonProgressButton) {
            autoLoading = getBoolean(R.styleable.ProtonProgressButton_autoLoading, autoLoading)
            progressDrawableId = getResourceId(R.styleable.ProtonProgressButton_progressDrawable, progressDrawableId)
            getInteger(R.styleable.ProtonProgressButton_initialState, -1).let { value ->
                State.values().find { it.i == value }?.let(::setState)
            }
        }
        // Set empty click listener, that will trigger the loading state
        if (autoLoading) setLoading()
    }

    /**
     * Sets the [State] to [LOADING]. Indefinite loading spinner will be visible until [loadingComplete] or [setIdle]
     * is called.
     */
    fun setLoading() {
        setState(LOADING)
    }

    /**
     * Sets the [State] to [IDLE]. The loading spinner will no longer be visible.
     */
    fun setIdle() {
        setState(IDLE)
    }

    /**
     * Sets the [State] of the button and updates the UI accordingly for the selected state.
     * @param state the new state that the button should be in.
     */
    private fun setState(state: State) {
        currentState = state
        when (state) {
            IDLE -> {
                isActivated = false
                isClickable = true
                setCompoundDrawables(null, null, null, null)
                setPadding(paddingRight, paddingTop, paddingRight, paddingBottom)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) progressDrawable.reset()
                else progressDrawable.stop()
            }
            LOADING -> {
                setCompoundDrawablesWithIntrinsicBounds(null, null, progressDrawable, null)
                setPadding(totalPaddingRight, paddingTop, paddingRight, paddingBottom)
                progressDrawable.start()
                isActivated = true
                isClickable = false
            }
        }
    }

    override fun loadingComplete() {
        setIdle()
    }

    /** Sets the click listener. */
    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener {
            if (autoLoading) setLoading()
            listener?.onClick(it)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setIdle()
    }

    /**
     * Represents the state of the button. It can be [IDLE] to act as a normal button, but also [LOADING] for a
     * long running operations.
     */
    enum class State(internal val i: Int) {
        IDLE(0),
        LOADING(1)
    }
}
