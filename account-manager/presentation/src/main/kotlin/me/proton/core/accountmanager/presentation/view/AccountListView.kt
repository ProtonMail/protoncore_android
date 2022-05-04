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

package me.proton.core.accountmanager.presentation.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.presentation.R
import me.proton.core.accountmanager.presentation.adapter.AccountListItemAdapter
import me.proton.core.accountmanager.presentation.databinding.AccountListViewBinding
import me.proton.core.accountmanager.presentation.entity.AccountListItem
import me.proton.core.accountmanager.presentation.viewmodel.AccountSwitcherViewModel

@SuppressLint("RestrictedApi")
class AccountListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {
    private var accountsObserver: Job? = null

    private val binding = AccountListViewBinding.inflate(LayoutInflater.from(context), this, true)

    private val accountListItemAdapter: AccountListItemAdapter = AccountListItemAdapter()

    private var viewModel: AccountSwitcherViewModel? = null

    init {
        binding.accountListRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = accountListItemAdapter
            addItemDecoration(accountListItemAdapter.ItemDecoration(context, RecyclerView.VERTICAL))
        }

        accountListItemAdapter.setOnListItemClicked {
            when (it) {
                is AccountListItem.Section.SwitchTo -> Unit
                is AccountListItem.Account -> viewModel?.switch(it.accountItem.userId)
                is AccountListItem.Action.AddAccount -> viewModel?.add()
            }
        }

        accountListItemAdapter.setOnAccountMenuInflated { account, menu ->
            if (menu is MenuBuilder) {
                menu.setOptionalIconsVisible(true)
            }
            val menuSignIn = menu.findItem(R.id.account_menu_sign_in)
            val menuSignOut = menu.findItem(R.id.account_menu_sign_out)
            val menuRemove = menu.findItem(R.id.account_menu_remove)
            when (account.accountItem.state) {
                AccountState.Ready -> {
                    menuSignIn.isVisible = false
                    menuSignOut.isVisible = true
                    menuRemove.isVisible = true
                }
                AccountState.Disabled -> {
                    menuSignIn.isVisible = true
                    menuSignOut.isVisible = false
                    menuRemove.isVisible = true
                }
                else -> Unit
            }
        }

        accountListItemAdapter.setOnAccountMenuItemClicked { account, menuItem ->
            when (menuItem.itemId) {
                R.id.account_menu_sign_in -> viewModel?.switch(account.accountItem.userId)
                R.id.account_menu_sign_out -> viewModel?.signOut(account.accountItem.userId)
                R.id.account_menu_remove -> viewModel?.remove(account.accountItem.userId)
            }
            true
        }
    }

    fun setViewModel(viewModel: AccountSwitcherViewModel?) {
        this.viewModel = viewModel
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.observeAccounts()
    }

    override fun onDetachedFromWindow() {
        accountsObserver?.cancel()
        super.onDetachedFromWindow()
    }

    private fun LifecycleOwner.observeAccounts() {
        accountsObserver?.cancel()
        accountsObserver = (viewModel?.accounts ?: flowOf(emptyList()))
            .flowWithLifecycle(lifecycle)
            .onEach { accounts ->
                val primary = accounts.firstOrNull { it is AccountListItem.Account.Primary }
                val ready = accounts.filterIsInstance<AccountListItem.Account.Ready>()
                val disabled = accounts.filterIsInstance<AccountListItem.Account.Disabled>()
                val other = ready + disabled

                val list = mutableListOf<AccountListItem>()
                primary?.let { list.add(primary) }
                other.takeIf { it.isNotEmpty() }?.let {
                    list.add(AccountListItem.Section.SwitchTo)
                    list.addAll(other)
                }
                list.add(AccountListItem.Action.AddAccount)
                accountListItemAdapter.submitList(list)
            }.launchIn(lifecycleScope)
    }

    companion object {
        fun createDialog(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            viewModel: AccountSwitcherViewModel?
        ): AlertDialog =
            MaterialAlertDialogBuilder(context)
                .setView(AccountListView(context).apply {
                    ViewTreeLifecycleOwner.set(this, lifecycleOwner)
                    setViewModel(viewModel)
                })
                .create().apply { window?.setGravity(Gravity.TOP) }
    }
}
