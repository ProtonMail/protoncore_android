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

package me.proton.core.accountmanager.presentation.entity

import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import me.proton.core.accountmanager.presentation.R

sealed class AccountListItem(
    val type: Type
) {
    enum class Type {
        Account,
        Section,
        Action
    }

    sealed class Account(open val accountItem: AccountItem) : AccountListItem(Type.Account) {
        data class Primary(override val accountItem: AccountItem) : Account(accountItem)
        data class Ready(override val accountItem: AccountItem) : Account(accountItem)
        data class Disabled(override val accountItem: AccountItem) : Account(accountItem)

        companion object {
            @MenuRes
            val menuResId = R.menu.account_menu
        }
    }

    sealed class Section(@StringRes val textResId: Int) : AccountListItem(Type.Section) {
        object SwitchTo : Section(
            textResId = R.string.account_switcher_section_switch_to
        )
    }

    sealed class Action(@StringRes val textResId: Int, @DrawableRes val iconResId: Int) : AccountListItem(Type.Action) {
        object AddAccount : Action(
            textResId = R.string.account_switcher_action_add_account,
            iconResId = R.drawable.ic_person_add,
        )
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<AccountListItem>() {
            override fun areItemsTheSame(oldItem: AccountListItem, newItem: AccountListItem) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: AccountListItem, newItem: AccountListItem) =
                oldItem == newItem
        }
    }
}
