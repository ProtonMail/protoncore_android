package me.proton.android.core.presentation.ui.adapter

import android.widget.Filter
import android.widget.Filterable
import kotlin.collections.filter as kotlinFilter // This is needed, otherwise it would clash with `Filter` APIs

/**
 * Common interface for Adapters that are filterable
 */
interface FilterableAdapter<UiModel, ModelsList : List<UiModel>> : Filterable {

    /**
     * A reference to all the items without any filter applied.
     * This must be be updated in sub-classes, every time an update for items is received.
     * e.g. override [androidx.recyclerview.widget.ListAdapter.submitList] for Adapters that inherit from
     * [androidx.recyclerview.widget.ListAdapter]
     */
    var unfilteredList: ModelsList

    /**
     * Perform a filter on a single element of the [unfilteredList]
     * Default doesn't apply any filter
     */
    fun onFilter(element: UiModel, constraint: CharSequence): Boolean = true

    /**
     * Publish filtered results as list
     */
    fun submitFilteredList(list: ModelsList)

    override fun getFilter() = object : Filter() {

        override fun performFiltering(constraint: CharSequence?) =
            FilterResults().apply { values = unfilteredList.kotlinFilter { onFilter(it, constraint ?: "") } }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            @Suppress("UNCHECKED_CAST")
            submitFilteredList(results.values as ModelsList)
        }
    }
}
