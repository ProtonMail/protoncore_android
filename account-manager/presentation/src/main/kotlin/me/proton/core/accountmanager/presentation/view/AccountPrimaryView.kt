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

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.accountmanager.presentation.R
import me.proton.core.accountmanager.presentation.databinding.AccountPrimaryViewBinding
import me.proton.core.accountmanager.presentation.viewmodel.AccountSwitcherViewModel
import me.proton.core.presentation.utils.onClick
import javax.inject.Inject

@AndroidEntryPoint
class AccountPrimaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = AccountPrimaryViewBinding.inflate(LayoutInflater.from(context), this, true)

    @Inject
    lateinit var viewModel: AccountSwitcherViewModel

    var initials: String?
        get() = binding.accountInitialsTextview.text.toString()
        set(value) {
            binding.accountInitialsTextview.text = value
        }

    var name: String?
        get() = binding.accountNameTextview.text.toString()
        set(value) {
            binding.accountNameTextview.text = value
        }

    var email: String?
        get() = binding.accountEmailTextview.text.toString()
        set(value) {
            binding.accountEmailTextview.text = value
        }

    var isExpandable: Boolean
        get() = binding.accountExpandImageview.isVisible
        set(value) {
            binding.accountExpandImageview.isVisible = value
        }

    var isAutoUpdated: Boolean = true

    init {
        context.withStyledAttributes(attrs, R.styleable.AccountPrimaryView) {
            initials = getString(R.styleable.AccountPrimaryView_initials)
            name = getString(R.styleable.AccountPrimaryView_name)
            email = getString(R.styleable.AccountPrimaryView_email)
            isExpandable = getBoolean(R.styleable.AccountPrimaryView_isExpandable, true)
            isAutoUpdated = getBoolean(R.styleable.AccountPrimaryView_isAutoUpdated, true)
        }

        binding.root.onClick {
            if (isExpandable) {
                MaterialAlertDialogBuilder(context)
                    .setView(AccountListView(context))
                    .show()
                    .window?.setGravity(Gravity.TOP)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        viewModel.primaryAccount
            .filter { isAutoUpdated }
            .onEach { account ->
                initials = account?.initials
                name = account?.name
                email = account?.email
            }.launchIn(viewModel.viewModelScope)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (isInEditMode) return

        viewModel.viewModelScope.cancel()
    }
}
