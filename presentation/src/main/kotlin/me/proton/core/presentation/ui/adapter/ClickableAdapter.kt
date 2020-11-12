package me.proton.core.presentation.ui.adapter

import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * A common interface for Adapters that have clickable items [UiModel]
 */
interface ClickableAdapter<UiModel> {

    /**
     * A callback that will be triggered when an item is clicked, has [UiModel] as lambda parameter
     */
    val onItemClick: (UiModel) -> Unit

    /**
     * A callback that will be triggered when an item is long clicked, has [UiModel] as lambda parameter
     */
    val onItemLongClick: (UiModel) -> Unit


    /**
     * Base [RecyclerView.ViewHolder] for [ClickableAdapter] implementations
     *
     * #### Generics
     * @param UiModel type of the model to be rendered
     * @param ViewRef type for the reference to the [View] to render on
     *   it could be a [View] or a ViewBinding reference
     *
     * #### Constructor parameters
     * @param viewRef reference to the view, type [ViewRef]
     * @param clickListener lambda triggered when an element is clicked, it receives [UiModel] as parameter
     * @param longClickListener lambda triggered when an element is long-clicked, it receives [UiModel] as parameter
     */
    abstract class ViewHolder<UiModel, ViewRef : Any>(
        protected val viewRef: ViewRef,
        protected val clickListener: (UiModel) -> Unit,
        protected val longClickListener: (UiModel) -> Unit = {}
    ) : RecyclerView.ViewHolder(viewRef.getView()) {

        protected val context: Context get() = itemView.context

        /** Populate the [View] with the given [item] [UiModel] */
        open fun onBind(item: UiModel) {
            itemView.setOnClickListener { clickListener(item) }
            itemView.setOnLongClickListener {
                longClickListener(item)
                true
            }
        }

        @ColorInt
        protected fun getColor(@ColorRes resId: Int) =
            ContextCompat.getColor(context, resId)

        protected fun getDrawable(@DrawableRes resId: Int) =
            ContextCompat.getDrawable(context, resId)!!

        protected fun getString(@StringRes resId: Int): CharSequence =
            context.getString(resId)

        protected fun getText(@StringRes resId: Int): CharSequence =
            context.getText(resId)


        protected companion object {
            /**
             * @return [View] from receiver [V]
             * @param V must be a [View] or a ViewBinding references
             *
             * @throws IllegalArgumentException if [V] constraints are not satisfied
             */
            fun <V : Any> V.getView(): View {
                val viewRefClass = this::class

                return if (this is View) this
                else viewRefClass.members.find { it.name == "getRoot" }?.call(this) as? View
                    ?: throw IllegalArgumentException("Impossible to get a View for ViewHolder from constructor " +
                        "parameter of type ${viewRefClass.simpleName}, use a View or a ViewBinding reference")
            }
        }
    }
}
