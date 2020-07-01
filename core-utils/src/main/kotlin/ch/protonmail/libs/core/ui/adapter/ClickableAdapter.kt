@file:Suppress("unused", "MemberVisibilityCanBePrivate") // Public APIs

package ch.protonmail.libs.core.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * A common interface for Adapters that has clickable items [T]
 * @author Davide Farella
 */
interface ClickableAdapter<T, VH : ClickableAdapter.ViewHolder<T>> {

    /** A callback that will be triggered when an item is clicked */
    var onItemClick: (T) -> Unit

    /**
     * An invoker for [onItemClick], we use it so the [ViewHolder] will always use the updated
     * [onItemClick] even if it changes after the [ViewHolder] is created.
     */
    private val clickListenerInvoker: (T) -> Unit get() = { onItemClick(it) }

    /** A callback that will be triggered when an item is long clicked */
    var onItemLongClick: (T) -> Unit

    /**
     * An invoker for [onItemLongClick], we use it so the [ViewHolder] will always use the updated
     * [onItemLongClick] even if it changes after the [ViewHolder] is created.
     */
    private val longClickListenerInvoker: (T) -> Unit get() = { onItemLongClick(it) }

    /** Prepare the given [ViewHolder] with click listeners */
    private fun prepareViewHolder(holder: VH) {
        holder.clickListenerInvoker = this.clickListenerInvoker
        holder.longClickListenerInvoker = this.longClickListenerInvoker
    }

    /** Calls private [prepareViewHolder] on the receiver [ViewHolder] */
    fun prepareClickListeners(holder: VH) {
        prepareViewHolder(holder)
    }

    /** A base [RecyclerView.ViewHolder] for [ClickableAdapter] implementations */
    abstract class ViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /** @return [Context] from [itemView]  */
        protected val context: Context get() = itemView.context

        internal var clickListenerInvoker: (T) -> Unit = {}
        internal var longClickListenerInvoker: (T) -> Unit = {}

        /** Populate the [View] with the given [item] [T] */
        open fun onBind(item: T) {
            itemView.setOnClickListener { clickListenerInvoker(item) }
            itemView.setOnLongClickListener {
                longClickListenerInvoker(item)
                true
            }
        }

        /** @return a [ColorInt] from [context] */
        @ColorInt
        protected fun getColor(@ColorRes resId: Int) =
                ContextCompat.getColor(context, resId)

        /** @return a [Drawable] from [context] */
        protected fun getDrawable(@DrawableRes resId: Int) =
                ContextCompat.getDrawable(context, resId)!!

        /** @return a [CharSequence] String from [context] */
        protected fun getString(@StringRes resId: Int): CharSequence =
                context.getString(resId)

        /** @return a [CharSequence] Text from [context] */
        protected fun getText(@StringRes resId: Int): CharSequence =
                context.getText(resId)
    }
}
