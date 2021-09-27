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
import me.proton.android.core.coreexample.databinding.ItemContactBinding
import me.proton.core.contact.domain.entity.Contact
import me.proton.core.contact.domain.entity.ContactId

class ContactsAdapter(
    private val onClickListener: (ContactId) -> Unit
) : ListAdapter<Contact, ContactViewHolder>(ContactDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.create(parent, onClickListener)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ContactViewHolder(
    private val binding: ItemContactBinding,
    private val onClickListener: (ContactId) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(contact: Contact) {
        binding.root.setOnClickListener { onClickListener(contact.id) }
        binding.name.text = contact.name
        binding.emails.text = contact.contactEmails.joinToString { it.email }
    }

    companion object {
        fun create(parent: ViewGroup, onClickListener: (ContactId) -> Unit) = ContactViewHolder(
            binding = ItemContactBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onClickListener = onClickListener
        )
    }
}

class ContactDiffUtilCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem == newItem
    }
}