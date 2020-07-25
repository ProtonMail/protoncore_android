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

package me.proton.core.humanverification.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.proton.android.core.presentation.utils.onClick
import me.proton.core.humanverification.presentation.databinding.ItemCountryBinding
import me.proton.core.humanverification.presentation.entity.CountryUIModel
import me.proton.core.util.kotlin.containsNoCase
import kotlin.collections.ArrayList

/**
 * @author Dino Kadrikj.
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
), Filterable {

    private var filteredList: List<CountryUIModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredList[position]

        holder.bind(item)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun submitList(list: List<CountryUIModel>?) {
        super.submitList(list)
        filteredList = list ?: emptyList()
    }

    class ViewHolder(
        private val itemBinding: ItemCountryBinding,
        private val clickListener: (CountryUIModel) -> Unit
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: CountryUIModel) {
            itemBinding.apply {
                root.onClick { clickListener(item) }
                callingCodeText.text = "+${item.callingCode}"
                name.text = item.name
                flag.setImageResource(item.flagId)
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint?.toString()
                filteredList = if (charSearch.isNullOrEmpty()) {
                    currentList
                } else {
                    val resultList = ArrayList<CountryUIModel>()
                    for (country in currentList) {
                        if (country.name.containsNoCase(charSearch)) {
                            resultList.add(country)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<CountryUIModel>
                notifyDataSetChanged()
            }

        }
    }
}
