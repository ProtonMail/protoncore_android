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

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil

/**
 * A [PagedListAdapter] that contains a [List] of [UiModel] items.
 * Inherit from [PagedListAdapter].
 *
 * A basic implementations expects
 * * an implementation of [DiffUtil.ItemCallback] that will be passed to the constructor
 * * an implementation of [ClickableAdapter.ViewHolder] that will be passes ad generic [UiModel] and
 * will be created by overriding [onCreateViewHolder]
 *
 * A basic usage only require [PagedListAdapter.submitList] to be set and [DiffUtil] will handle
 * everything.
 * [ClickableAdapter.onItemClick] and [ClickableAdapter.onItemLongClick] can be set.
 *
 *
 * @param itemCallback a REQUIRED [DiffUtil.ItemCallback] of [UiModel] that will be used
 * for compare the items.
 *
 *
 * @author Davide Giuseppe Farella.
 */
abstract class BasePagedAdapter<UiModel, ViewHolder : ClickableAdapter.ViewHolder<UiModel>>(
    itemCallback: DiffUtil.ItemCallback<UiModel>,
    @set:Deprecated("This should not be mutable. It will be immutable from 0.3.x. Move it in the " +
            "constructor if didn't do yet")
    override var onItemClick: (UiModel) -> Unit = {},
    @set:Deprecated("This should not be mutable. It will be immutable from 0.3.x. Move it in the " +
            "constructor if didn't do yet")
    override var onItemLongClick: (UiModel) -> Unit = {}
) : PagedListAdapter<UiModel, ViewHolder>(itemCallback), ClickableAdapter<UiModel, ViewHolder> {


    /** @see PagedListAdapter.onBindViewHolder */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.onBind(it)
            prepareClickListeners(holder)
        }
    }
}
