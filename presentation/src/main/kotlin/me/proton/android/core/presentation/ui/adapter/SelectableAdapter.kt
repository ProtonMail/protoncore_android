@file:Suppress("unused") // Public APIs

package me.proton.android.core.presentation.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * A common interface for Adapters that has clickable and selectable items [T]
 * Inherit from [ClickableAdapter]
 *
 * @author Davide Farella
 */
interface SelectableAdapter<T, VH : SelectableAdapter.ViewHolder<T>> : ClickableAdapter<T, VH> {

    /** A callback that will be triggered when an item selection is changed */
    var onItemSelect: (T, isSelected: Boolean) -> Unit

    /**
     * An invoker for [onItemSelect], we use it so the [ViewHolder] will always use the updated
     * [onItemSelect] even if it changes after the [ViewHolder] is created.
     */
    private val selectListenerInvoker: (T, isSelected: Boolean) -> Unit
        get() =
            { item: T, isSelected: Boolean -> onItemSelect(item, isSelected) }

    /** Prepare the given [ViewHolder] with select listener */
    private fun prepareSelectListener(holder: VH) {
        holder.selectListenerInvoker = this.selectListenerInvoker
    }

    /** Calls private prepareViewHolder on the receiver [ViewHolder] */
    override fun prepareClickListeners(holder: VH) {
        super.prepareClickListeners(holder)
        prepareSelectListener(holder)
    }

    /**
     * A base [RecyclerView.ViewHolder] for [SelectableAdapter] implementations
     * Inherit from [ClickableAdapter.ViewHolder]
     */
    abstract class ViewHolder<T>(itemView: View)
        : ClickableAdapter.ViewHolder<T>(itemView) {

        internal var selectListenerInvoker: (T, isSelected: Boolean) -> Unit = { _, _ -> }

        /** Trigger the selection listener */
        protected fun setSelected(item: T, isSelected: Boolean) {
            selectListenerInvoker(item, isSelected)
        }
    }
}
