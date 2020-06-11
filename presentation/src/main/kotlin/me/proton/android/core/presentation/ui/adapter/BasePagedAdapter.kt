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
