@file:Suppress("MaxLineLength") // This must be removed after the code is uncommented

package me.proton.android.core.presentation.ui.adapter

// TODO: This must be implemented, but needs more work than 'ProtonAdapter'
//  I chose to leave this interface here, just to serve as reminder for when anybody will try to create a class that
//  should implements ProtonPagedAdapter
interface ProtonPagedAdapter

/**
 * A [PagingDataAdapter] for [RecyclerView] that contains Data of [UiModel] items.
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
// abstract class ProtonPagedAdapter<UiModel : Any, ViewRef : Any, ViewHolder : ClickableAdapter.ViewHolder<UiModel, ViewRef>>(
//     override val onItemClick: (UiModel) -> Unit,
//     override val onItemLongClick: (UiModel) -> Unit = {},
//     diffCallback: DiffUtil.ItemCallback<UiModel>
// ) : PagingDataAdapter<UiModel, ViewHolder>(
//     diffCallback
// ), ClickableAdapter<UiModel>, FilterableAdapter<UiModel, List<UiModel>> {
//
//     final override var unfilteredList = emptyList<UiModel>()
//
//     /**
//      * Trigger binding of given [holder], with item at given [position]
//      */
//     override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//         val item = getItem(position)
//         holder.onBind(item)
//     }
//
//     /**
//      * Submit a [List] and updates [unfilteredList]
//      */
//     final override fun submitData(list: List<UiModel>?) {
//         super.submitList(list)
//         unfilteredList = list ?: emptyList()
//     }
//
//     /**
//      * Submit a [List] without updating [unfilteredList]
//      */
//     final override fun submitFilteredList(list: List<UiModel>) {
//         val backup = unfilteredList
//         submitList(list)
//         unfilteredList = backup
//     }
// }
//
// /**
//  * Create a [ProtonPagedAdapter] in functional way
//  * @param getView receives a [ViewGroup] and a [LayoutInflater].
//  *   It must return a [View] or a ViewBinding reference of type [ViewRef]
//  * @param onBind executes the binding on the [ViewRef], it receives [ViewRef] and [UiModel]
//  * @param onFilter ( OPTIONAL ) declares the logic for filter [UiModel] elements
//  *
//  * Other params inherit from [ProtonPagedAdapter] class
//  *
//  * @return [ProtonPagedAdapter]
//  */
// fun <UiModel, ViewRef : Any> ProtonPagedAdapter(
//     getView: (parent: ViewGroup, inflater: LayoutInflater) -> ViewRef,
//     onBind: (viewRef: ViewRef, uiModel: UiModel) -> Unit,
//     onItemClick: (UiModel) -> Unit,
//     onItemLongClick: (UiModel) -> Unit = {},
//     diffCallback: DiffUtil.ItemCallback<UiModel>,
//     onFilter: (element: UiModel, constraint: CharSequence) -> Boolean = { _, _ -> true }
// ) = object : ProtonPagedAdapter<UiModel, ViewRef, ClickableAdapter.ViewHolder<UiModel, ViewRef>>(
//     onItemClick, onItemLongClick, diffCallback
// ) {
//     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
//         object : ClickableAdapter.ViewHolder<UiModel, ViewRef>(
//             getView(parent, LayoutInflater.from(parent.context)),
//             onItemClick,
//             onItemLongClick
//         ) {
//             override fun onBind(item: UiModel) {
//                 super.onBind(item)
//                 onBind(viewRef, item)
//             }
//         }
//
//     override fun onFilter(element: UiModel, constraint: CharSequence) = onFilter(element, constraint)
// }
//
// /**
//  * Create a [ProtonPagedAdapter] in functional way.
//  * @see [ProtonPagedAdapter] function, this only defer by receiving a [LayoutRes] [layoutId], instead that a lambda that
//  *   must return a [ViewRef]
//  */
// fun <UiModel, ViewRef : Any> ProtonPagedAdapter(
//     @LayoutRes layoutId: Int,
//     onBind: (viewRef: ViewRef, uiModel: UiModel) -> Unit,
//     onItemClick: (UiModel) -> Unit,
//     onItemLongClick: (UiModel) -> Unit = {},
//     diffCallback: DiffUtil.ItemCallback<UiModel>,
//     onFilter: (element: UiModel, constraint: CharSequence) -> Boolean = { _, _ -> true }
// ) = ProtonPagedAdapter(
//     { parent, _ ->
//         @Suppress("UNCHECKED_CAST")
//         parent.inflate(layoutId) as ViewRef
//     },
//     onBind,
//     onItemClick,
//     onItemLongClick,
//     diffCallback,
//     onFilter
// )
