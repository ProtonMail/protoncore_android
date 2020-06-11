package me.proton.core.humanverification.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.proton.android.core.presentation.utils.onClick
import me.proton.core.humanverification.presentation.databinding.ItemCountryBinding
import me.proton.core.humanverification.presentation.entity.CountryUIModel

/**
 * Created by dinokadrikj on 6/17/20.
 */
class CountriesListAdapter(
    private val clickListener: (CountryUIModel) -> Unit
) : ListAdapter<CountryUIModel, CountriesListAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<CountryUIModel>() {
        override fun areItemsTheSame(oldItem: CountryUIModel, newItem: CountryUIModel) =
            oldItem.countryCode == newItem.countryCode

        override fun areContentsTheSame(oldItem: CountryUIModel, newItem: CountryUIModel) =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val itemBinding: ItemCountryBinding,
        private val clickListener: (CountryUIModel) -> Unit
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: CountryUIModel) {
            itemBinding.apply {
                root.onClick { clickListener(item) }
                callingCodeText.text = item.countryCode
                name.text = item.name
            }
        }
    }
}
