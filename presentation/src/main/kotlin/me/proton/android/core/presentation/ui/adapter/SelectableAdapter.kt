package me.proton.android.core.presentation.ui.adapter

import androidx.recyclerview.widget.RecyclerView

/**
 * A common interface for Adapters that have clickable and selectable items [UiModel]
 * Inherit from [ClickableAdapter]
 */
interface SelectableAdapter<UiModel> : ClickableAdapter<UiModel> {

    /**
     * A callback that will be triggered when an item selection is changed, has [UiModel] and [Boolean] 'isSelected' as
     * lambda parameters
     */
    val onItemSelect: (UiModel, isSelected: Boolean) -> Unit


    /**
     * Base [RecyclerView.ViewHolder] for [SelectableAdapter] implementations
     * Inherit from [ClickableAdapter.ViewHolder]
     */
    abstract class ViewHolder<UiModel, ViewRef : Any>(
        viewRef: ViewRef,
        clickListener: (UiModel) -> Unit = {},
        longClickListener: (UiModel) -> Unit = {},
        protected val selectListener: (UiModel, Boolean) -> Unit
    ) : ClickableAdapter.ViewHolder<UiModel, ViewRef>(viewRef, clickListener, longClickListener) {

        /** Trigger the selection listener */
        protected fun setSelected(item: UiModel, isSelected: Boolean) {
            selectListener(item, isSelected)
        }
    }
}
