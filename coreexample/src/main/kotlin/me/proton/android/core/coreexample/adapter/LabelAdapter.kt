/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.android.core.coreexample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.proton.android.core.coreexample.databinding.ItemLabelBinding
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType

class LabelsAdapter(
    private val onClickListener: (LabelId, LabelType) -> Unit
) : ListAdapter<Label, LabelViewHolder>(LabelDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder =
        LabelViewHolder.create(parent, onClickListener)

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class LabelViewHolder(
    private val binding: ItemLabelBinding,
    private val onClickListener: (LabelId, LabelType) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Label) {
        binding.root.setOnClickListener { onClickListener(item.labelId, item.type) }
        binding.name.text = item.name
        binding.type.text = item.type.name
    }

    companion object {
        fun create(parent: ViewGroup, onClickListener: (LabelId, LabelType) -> Unit) = LabelViewHolder(
            binding = ItemLabelBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onClickListener = onClickListener
        )
    }
}

class LabelDiffUtilCallback : DiffUtil.ItemCallback<Label>() {
    override fun areItemsTheSame(oldItem: Label, newItem: Label): Boolean = oldItem.labelId == newItem.labelId
    override fun areContentsTheSame(oldItem: Label, newItem: Label): Boolean = oldItem == newItem
}
