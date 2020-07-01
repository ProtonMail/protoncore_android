@file:Suppress("unused") // Public APIs

package ch.protonmail.libs.core.ui.adapter

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
        itemCallback: DiffUtil.ItemCallback<UiModel>
) : PagedListAdapter<UiModel, ViewHolder>(itemCallback), ClickableAdapter<UiModel, ViewHolder> {

    /** A callback that will be triggered when an item is clicked */
    override var onItemClick: (UiModel) -> Unit = {}

    /** A callback that will be triggered when an item is long clicked */
    override var onItemLongClick: (UiModel) -> Unit = {}

    /** @see PagedListAdapter.onBindViewHolder */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.onBind(it)
            prepareClickListeners(holder)
        }
    }
}
