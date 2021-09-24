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

package me.proton.android.core.coreexample.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.R
import me.proton.android.core.coreexample.adapter.ContactsAdapter
import me.proton.android.core.coreexample.databinding.ActivityContactsBinding
import me.proton.android.core.coreexample.viewmodel.ContactsViewModel
import me.proton.core.contact.domain.entity.ContactId
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.showToast
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class ContactsActivity : ProtonActivity<ActivityContactsBinding>() {
    override fun layoutId(): Int = R.layout.activity_contacts

    private val viewModel: ContactsViewModel by viewModels()
    private val contactsAdapter = ContactsAdapter(::onClickContact)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.contactsRecyclerView.adapter = contactsAdapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is ContactsViewModel.State.Contacts -> contactsAdapter.submitList(state.contacts)
                        is ContactsViewModel.State.Error -> showToast(state.reason)
                        ContactsViewModel.State.Processing -> showToast("processing")
                    }.exhaustive
                }
            }
        }
    }

    private fun onClickContact(contactId: ContactId) {
        startActivity(ContactDetailActivity.createIntent(this, contactId))
    }
}
