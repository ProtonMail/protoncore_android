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

package me.proton.core.accountmanager.presentation.adapter

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.presentation.databinding.AccountActionBinding
import me.proton.core.accountmanager.presentation.databinding.AccountSectionBinding
import me.proton.core.accountmanager.presentation.databinding.AccountViewBinding
import me.proton.core.accountmanager.presentation.entity.AccountListItem
import me.proton.core.presentation.utils.onClick

class AccountListItemAdapter : ListAdapter<AccountListItem, AccountListItemAdapter.ViewHolder>(
    AsyncDifferConfig.Builder(AccountListItem.DiffCallback).build()
) {

    init {
        setHasStableIds(true)
    }

    private var onListItemClicked: ((AccountListItem) -> Unit)? = null
    private var onAccountMenuInflated: ((AccountListItem.Account, Menu) -> Unit)? = null
    private var onAccountMenuItemClicked: ((AccountListItem.Account, MenuItem) -> Boolean)? = null

    override fun getItemId(position: Int): Long = getItem(position).hashCode().toLong()

    override fun getItemViewType(position: Int): Int = getItem(position).type.ordinal

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            AccountListItem.Type.Account.ordinal -> AccountViewBinding.inflate(inflater, parent, false)
            AccountListItem.Type.Section.ordinal -> AccountSectionBinding.inflate(inflater, parent, false)
            AccountListItem.Type.Action.ordinal -> AccountActionBinding.inflate(inflater, parent, false)
            else -> null
        }
        return ViewHolder(requireNotNull(view))
    }

    fun setOnListItemClicked(block: (AccountListItem) -> Unit) {
        onListItemClicked = block
    }

    fun setOnAccountMenuInflated(block: (AccountListItem.Account, Menu) -> Unit) {
        onAccountMenuInflated = block
    }

    fun setOnAccountMenuItemClicked(block: (AccountListItem.Account, MenuItem) -> Boolean) {
        onAccountMenuItemClicked = block
    }

    inner class ViewHolder(private var binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        private fun ViewBinding.getText(@StringRes textResId: Int) = root.context.getString(textResId)

        fun bind(item: AccountListItem) {
            when (item) {
                is AccountListItem.Account -> bind(item, binding as AccountViewBinding)
                is AccountListItem.Section -> bind(item, binding as AccountSectionBinding)
                is AccountListItem.Action -> bind(item, binding as AccountActionBinding)
            }
            binding.root.onClick { onListItemClicked?.invoke(item) }
        }

        fun bind(item: AccountListItem.Account, binding: AccountViewBinding) {
            val isReady = item.accountItem.state == AccountState.Ready
            binding.accountInitialsTextview.text = item.accountItem.initials
            binding.accountEmailTextview.text = item.accountItem.email
            binding.accountNameTextview.text = item.accountItem.name
            binding.accountEmailTextview.isEnabled = isReady
            binding.accountNameTextview.isEnabled = isReady
            binding.accountMoreButton.onClick {
                PopupMenu(binding.root.context, binding.accountMoreButton).apply {
                    inflate(AccountListItem.Account.menuResId)
                    onAccountMenuInflated?.invoke(item, menu)
                    setOnMenuItemClickListener { onAccountMenuItemClicked?.invoke(item, it) ?: false }
                }.show()
            }
        }

        fun bind(item: AccountListItem.Section, binding: AccountSectionBinding) {
            binding.accountSectionTextview.text = binding.getText(item.textResId)
        }

        fun bind(item: AccountListItem.Action, binding: AccountActionBinding) {
            binding.accountActionTextview.text = binding.getText(item.textResId)
            binding.accountActionIcon.setImageResource(item.iconResId)
        }
    }

    inner class ItemDecoration(
        context: Context,
        orientation: Int
    ) : DividerItemDecoration(context, orientation) {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position == -1) return
            val decorateItem = when (getItemViewType(position)) {
                AccountListItem.Type.Account.ordinal -> true
                AccountListItem.Type.Section.ordinal -> false
                AccountListItem.Type.Action.ordinal -> false
                else -> false
            }
            if (decorateItem) {
                super.getItemOffsets(outRect, view, parent, state)
            } else {
                outRect.setEmpty()
            }
        }
    }
}
