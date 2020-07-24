/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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
    abstract class ViewHolder<T>(itemView: View) : ClickableAdapter.ViewHolder<T>(itemView) {

        internal var selectListenerInvoker: (T, isSelected: Boolean) -> Unit = { _, _ -> }

        /** Trigger the selection listener */
        protected fun setSelected(item: T, isSelected: Boolean) {
            selectListenerInvoker(item, isSelected)
        }
    }
}
