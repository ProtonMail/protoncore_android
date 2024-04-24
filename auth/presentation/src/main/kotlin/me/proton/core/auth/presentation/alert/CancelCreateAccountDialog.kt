/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.auth.presentation.alert

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.DialogCancelCreationBinding
import me.proton.core.auth.presentation.viewmodel.CancelCreateAccountDialogViewModel
import me.proton.core.presentation.utils.onClick

@AndroidEntryPoint
class CancelCreateAccountDialog : DialogFragment() {

    private val viewModel by viewModels<CancelCreateAccountDialogViewModel>()

    private var onCreationCancelled: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val binding = DialogCancelCreationBinding.inflate(LayoutInflater.from(requireContext()))
        // Buttons are in a custom View to avoid dismissing dialog on click (cancel scope).
        binding.continueButton.onClick {
            dismiss()
        }
        binding.cancelButton.onClick {
            viewModel.cancelCreation().invokeOnCompletion {
                dismiss()
                onCreationCancelled?.invoke()
            }
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.auth_signup_create_account_dialog_title)
            .setMessage(R.string.auth_signup_create_account_dialog_text)
            .setView(binding.root)
            .create()
    }

    fun show(fragmentManager: FragmentManager, onCreationCancelled: () -> Unit) {
        this.onCreationCancelled = onCreationCancelled
        show(fragmentManager, null)
    }
}
