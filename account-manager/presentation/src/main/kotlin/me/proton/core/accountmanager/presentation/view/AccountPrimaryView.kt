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
import android.view.LayoutInflater
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.accountmanager.presentation.R
import me.proton.core.accountmanager.presentation.databinding.AccountPrimaryViewBinding
import me.proton.core.accountmanager.presentation.viewmodel.AccountSwitcherViewModel

class AccountPrimaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = AccountPrimaryViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var onViewClicked: (() -> Unit)? = null
    private var onDialogShown: (() -> Unit)? = null
    private var onDialogDismissed: (() -> Unit)? = null

    private var viewModel: AccountSwitcherViewModel? = null

    private var dialog: AlertDialog? = null

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

    var isDialogEnabled: Boolean
        get() = binding.accountExpandImageview.isVisible
        set(value) {
            binding.accountExpandImageview.isVisible = value
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.AccountPrimaryView) {
            initials = getString(R.styleable.AccountPrimaryView_initials)
            name = getString(R.styleable.AccountPrimaryView_name)
            email = getString(R.styleable.AccountPrimaryView_email)
            isDialogEnabled = getBoolean(R.styleable.AccountPrimaryView_isDialogEnabled, true)
        }

        binding.root.setOnClickListener {
            if (onViewClicked == null && isDialogEnabled) {
                showDialog()
            } else {
                onViewClicked?.invoke()
            }
        }
    }

    fun setOnViewClicked(block: (() -> Unit)?) {
        onViewClicked = block
    }

    fun setOnDialogShown(block: (() -> Unit)?) {
        onDialogShown = block
    }

    fun setOnDialogDismissed(block: (() -> Unit)?) {
        onDialogDismissed = block
    }

    fun setViewModel(viewModel: AccountSwitcherViewModel?) {
        this.viewModel = viewModel

        viewModel?.apply {
            primaryAccount.onEach { account ->
                initials = account?.initials
                name = account?.name
                email = account?.email
            }.launchIn(viewModelScope)
        }
    }

    fun showDialog() {
        if (dialog == null) {
            dialog = AccountListView.createDialog(context, viewModel)
            dialog?.setOnDismissListener { onDialogDismissed?.invoke() }
            dialog?.setOnShowListener { onDialogShown?.invoke() }
        }
        dialog?.show()
    }

    fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }

    override fun onDetachedFromWindow() {
        dismissDialog()
        super.onDetachedFromWindow()
    }
}
