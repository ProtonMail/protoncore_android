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
import me.proton.android.core.coreexample.databinding.ItemPushBinding
import me.proton.android.core.coreexample.utils.prettyPrint
import me.proton.core.domain.entity.UserId
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId

class PushAdapter(
    private val onClickListener: (UserId, PushId) -> Unit
) : ListAdapter<Push, PushViewHolder>(PushDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PushViewHolder =
        PushViewHolder.create(parent, onClickListener)

    override fun onBindViewHolder(holder: PushViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PushViewHolder(
    private val binding: ItemPushBinding,
    private val onClickListener: (UserId, PushId) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(push: Push) {
        binding.pushText.text = push.prettyPrint()
        binding.root.setOnClickListener { onClickListener(push.userId, push.pushId) }
    }

    companion object {
        fun create(parent: ViewGroup, onClickListener: (UserId, PushId) -> Unit) = PushViewHolder(
            binding = ItemPushBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onClickListener = onClickListener,
        )
    }
}

class PushDiffUtilCallback : DiffUtil.ItemCallback<Push>() {
    override fun areItemsTheSame(oldItem: Push, newItem: Push): Boolean = oldItem.pushId == newItem.pushId

    override fun areContentsTheSame(oldItem: Push, newItem: Push): Boolean = oldItem == newItem
}
