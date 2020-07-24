/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.humanverification.presentation.ui.verification

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.presentation.ui.ProtonDialogFragment
import me.proton.android.core.presentation.utils.onClick
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentCountryPickerBinding
import me.proton.core.humanverification.presentation.entity.CountryUIModel
import me.proton.core.humanverification.presentation.ui.adapter.CountriesListAdapter
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationSMSFragment.Companion.BUNDLE_KEY_COUNTRY
import me.proton.core.humanverification.presentation.ui.verification.HumanVerificationSMSFragment.Companion.KEY_COUNTRY_SELECTED
import me.proton.core.humanverification.presentation.utils.removeCountryPicker
import me.proton.core.humanverification.presentation.viewmodel.verification.CountryPickerViewModel
import java.util.Locale

/**
 * Fragment that lists the available countries taken from the data local layer.
 *
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
class CountryPickerFragment :
    ProtonDialogFragment<FragmentCountryPickerBinding>() {

    private val viewModel by viewModels<CountryPickerViewModel>()

    private val countriesAdapter by lazy {
        CountriesListAdapter(::selectCountry)
    }

    override fun layoutId(): Int = R.layout.fragment_country_picker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.countriesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = countriesAdapter
        }
        binding.closeButton.onClick(::onClose)
        binding.filterEditText.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                countriesAdapter.filter.filter(newText)
                return false
            }

        })
        viewModel.countries.observe(viewLifecycleOwner) {
            doOnData {
                val context = requireContext()
                countriesAdapter.submitList(it.map { country ->
                    val id: Int = context.resources.getIdentifier(
                        country.countryCode.toLowerCase(Locale.ROOT),
                        "drawable",
                        context.packageName
                    )
                    country.copy(flagId = id)
                })
            }
            doOnError {
                countriesAdapter.submitList(mutableListOf())
            }
        }
    }

    private fun selectCountry(country: CountryUIModel) {
        parentFragmentManager.setFragmentResult(
            KEY_COUNTRY_SELECTED, bundleOf(BUNDLE_KEY_COUNTRY to country)
        )
        dismissAllowingStateLoss()
    }

    private fun onClose() {
        removeCountryPicker()
    }

    override fun getStyleResource(): Int = R.style.ProtonTheme_Dialog_Picker

    override fun onBackPressed() {
        dismissAllowingStateLoss()
    }
}
