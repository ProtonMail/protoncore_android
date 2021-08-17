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

package me.proton.core.country.presentation.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.country.presentation.R
import me.proton.core.country.presentation.databinding.FragmentCountryPickerBinding
import me.proton.core.country.presentation.databinding.ItemCountryBinding
import me.proton.core.country.presentation.entity.CountryUIModel
import me.proton.core.country.presentation.viewmodel.CountryPickerViewModel
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.ui.adapter.ProtonAdapter
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.containsNoCase
import me.proton.core.util.kotlin.exhaustive
import java.util.Locale

/**
 * Fragment that lists the available countries taken from the data local layer.
 */
@AndroidEntryPoint
class CountryPickerFragment :
    ProtonDialogFragment<FragmentCountryPickerBinding>() {

    private val viewModel by viewModels<CountryPickerViewModel>()

    @SuppressLint("SetTextI18n")
    private val countriesAdapter = ProtonAdapter(
        { parent, inflater -> ItemCountryBinding.inflate(inflater, parent, false) },
        { country ->
            callingCodeText.visibility = withCallingCode
            callingCodeText.text = "+${country.callingCode}"
            name.text = country.name
            flag.setImageResource(country.flagId)
        },
        onFilter = { country, query -> country.name containsNoCase query },
        onItemClick = ::selectCountry,
        diffCallback = CountryUIModel.DiffCallback
    )

    private val withCallingCode: Int by lazy {
        if (requireArguments().get(ARG_CALLING_CODE) as Boolean? != false) View.VISIBLE else View.INVISIBLE
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
        viewModel.countries.onEach {
            when (it) {
                is CountryPickerViewModel.State.Idle ->  hideProgress()
                is CountryPickerViewModel.State.Success -> onCountriesSuccess(it.countries)
                is CountryPickerViewModel.State.Error -> {
                    countriesAdapter.submitList(mutableListOf())
                    hideProgress()
                }
            }.exhaustive
        }.launchIn(lifecycleScope)
    }

    private fun hideProgress() = with(binding) {
        progress.visibility = View.GONE
    }

    private fun onCountriesSuccess(countries: List<CountryUIModel>) {
        val context = requireContext()
        countriesAdapter.submitList(
            countries.map { country ->
                val id: Int = context.resources.getIdentifier(
                    country.countryCode.lowercase(Locale.ROOT),
                    "drawable",
                    context.packageName
                )
                country.copy(flagId = id)
            }
        )
        hideProgress()
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

    override fun onBackPressed() {
        dismissAllowingStateLoss()
    }

    companion object {
        const val KEY_COUNTRY_SELECTED = "key.country_selected"
        const val BUNDLE_KEY_COUNTRY = "bundle.country"
        private const val ARG_CALLING_CODE = "arg.callingCode"

        operator fun invoke(withCallingCode: Boolean) = CountryPickerFragment().apply {
            arguments = bundleOf(
                ARG_CALLING_CODE to withCallingCode
            )
        }
    }
}
