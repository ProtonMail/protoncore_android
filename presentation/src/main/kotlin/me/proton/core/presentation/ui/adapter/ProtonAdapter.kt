package me.proton.core.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.proton.core.presentation.utils.inflate
import kotlin.properties.Delegates.observable

/**
 * A [ListAdapter] for [RecyclerView] that contains a [List] of [UiModel] items.
 * Implements [ClickableAdapter]
 *
 * A basic implementations expects
 * * an implementation of [DiffUtil.ItemCallback] of [UiModel]
 * * an implementation of [ClickableAdapter.ViewHolder] that will be passes a generic [UiModel] and will be created by
 *   overriding [onCreateViewHolder]
 *
 *
 * @param onItemClick A lambda with [UiModel] as parameter that will be triggered when an item is clicked
 * @param onItemLongClick A lambda with [UiModel] as parameter that will be triggered when an item is long clicked
 * @param diffCallback a REQUIRED implementation on [DiffUtil.ItemCallback] of [UiModel] that will be used for compare
 *   the items.
 */
abstract class ProtonAdapter<UiModel, ViewRef : Any, ViewHolder : ClickableAdapter.ViewHolder<UiModel, ViewRef>>(
    override val onItemClick: (UiModel) -> Unit = {},
    override val onItemLongClick: (UiModel) -> Unit = {},
    diffCallback: DiffUtil.ItemCallback<UiModel>
) : ListAdapter<UiModel, ViewHolder>(
    AsyncDifferConfig.Builder<UiModel>(diffCallback).build()
), ClickableAdapter<UiModel, ViewRef>, FilterableAdapter<UiModel, List<UiModel>> {

    final override var unfilteredList = emptyList<UiModel>()

    /**
     * Trigger binding of given [holder], with item at given [position]
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item, position)
    }

    /**
     * Submit a [List] and update [unfilteredList]
     */
    final override fun submitList(list: List<UiModel>?) {
        super.submitList(list)
        unfilteredList = list ?: emptyList()
    }

    /**
     * Submit a [List] without updating [unfilteredList]
     */
    final override fun submitFilteredList(list: List<UiModel>) {
        val backup = unfilteredList
        submitList(list)
        unfilteredList = backup
    }
}

/**
 * Create a [ProtonAdapter] in functional way
 * @param getView receives a [ViewGroup] and a [LayoutInflater].
 *   It must return a [View] or a ViewBinding reference of type [ViewRef]
 * @param onBind executes the binding on the [ViewRef], it has [ViewRef] as lambda receiver and [UiModel] as lambda
 *   parameter
 * @param onFilter ( OPTIONAL ) declares the logic for filter [UiModel] elements
 *
 * Other params inherit from [ProtonAdapter] class
 *
 * @return [ProtonAdapter]
 */
fun <UiModel, ViewRef : Any> ProtonAdapter(
    getView: (parent: ViewGroup, inflater: LayoutInflater) -> ViewRef,
    onBind: ViewRef.(uiModel: UiModel) -> Unit,
    onItemClick: (UiModel) -> Unit = {},
    onItemLongClick: (UiModel) -> Unit = {},
    diffCallback: DiffUtil.ItemCallback<UiModel>,
    onFilter: (element: UiModel, constraint: CharSequence) -> Boolean = { _, _ -> true }
) = object : ProtonAdapter<UiModel, ViewRef, ClickableAdapter.ViewHolder<UiModel, ViewRef>>(
    onItemClick, onItemLongClick, diffCallback
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        object : ClickableAdapter.ViewHolder<UiModel, ViewRef>(
            getView(parent, LayoutInflater.from(parent.context)),
            onItemClick,
            onItemLongClick
        ) {
            override fun onBind(item: UiModel, position: Int) {
                super.onBind(item, position)
                onBind(viewRef, item)
            }
        }

    override fun onFilter(element: UiModel, constraint: CharSequence) = onFilter(element, constraint)
}

/**
 * Create a [ProtonAdapter] in functional way.
 * @see [ProtonAdapter] function, this only defer by receiving a [LayoutRes] [layoutId], instead that a lambda that
 *   must return a [ViewRef]
 */
fun <UiModel, ViewRef : Any> ProtonAdapter(
    @LayoutRes layoutId: Int,
    onBind: ViewRef.(uiModel: UiModel) -> Unit,
    onItemClick: (UiModel) -> Unit = {},
    onItemLongClick: (UiModel) -> Unit = {},
    diffCallback: DiffUtil.ItemCallback<UiModel>,
    onFilter: (element: UiModel, constraint: CharSequence) -> Boolean = { _, _ -> true }
) = ProtonAdapter(
    { parent, _ ->
        @Suppress("UNCHECKED_CAST")
        parent.inflate(layoutId) as ViewRef
    },
    onBind,
    onItemClick,
    onItemLongClick,
    diffCallback,
    onFilter
)

/**
 * Create a [ProtonAdapter] in functional way
 * @param getView receives a [ViewGroup] and a [LayoutInflater].
 *   It must return a [View] or a ViewBinding reference of type [ViewRef]
 * @param onBind executes the binding on the [ViewRef], it has [ViewRef] as lambda receiver and [UiModel] as lambda
 *   parameter
 * @param onFilter ( OPTIONAL ) declares the logic for filter [UiModel] elements
 *
 * Other params inherit from [ProtonAdapter] class
 *
 * @return [ProtonAdapter]
 */
fun <UiModel, ViewRef : Any> selectableProtonAdapter(
    getView: (parent: ViewGroup, inflater: LayoutInflater) -> ViewRef,
    onBind: ViewRef.(uiModel: UiModel, selected: Boolean) -> Unit,
    onItemClick: (UiModel) -> Unit = {},
    onItemLongClick: (UiModel) -> Unit = {},
    diffCallback: DiffUtil.ItemCallback<UiModel>,
    onFilter: (element: UiModel, constraint: CharSequence) -> Boolean = { _, _ -> true }
) = object : ProtonAdapter<UiModel, ViewRef, ClickableAdapter.ViewHolder<UiModel, ViewRef>>(
    onItemClick,
    onItemLongClick,
    diffCallback
) {

    var selectedPosition by observable(-1) { _, oldPos, newPos ->
        if (newPos in unfilteredList.indices) {
            notifyItemChanged(oldPos)
            notifyItemChanged(newPos)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        object : ClickableAdapter.ViewHolder<UiModel, ViewRef>(
            getView(parent, LayoutInflater.from(parent.context)),
            onItemClick,
            onItemLongClick
        ) {
            override fun onBind(item: UiModel, position: Int) {
                itemView.setOnClickListener {
                    selectedPosition = position
                    clickListener(item)
                }
                itemView.setOnLongClickListener {
                    longClickListener(item)
                    true
                }
                onBind(viewRef, item, position == selectedPosition)
            }
        }

    override fun onFilter(element: UiModel, constraint: CharSequence) = onFilter(element, constraint)
}
