package me.proton.core.humanverification.presentation.ui.verification

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_country_picker.*
import me.proton.android.core.presentation.ui.ProtonFragment
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentCountryPickerBinding
import me.proton.core.humanverification.presentation.entity.CountryUIModel
import me.proton.core.humanverification.presentation.ui.adapter.CountriesListAdapter
import me.proton.core.humanverification.presentation.viewmodel.verification.CountryPickerViewModel

/**
 * Created by dinokadrikj on 6/17/20.
 */
@AndroidEntryPoint
class CountryPickerFragment :
    ProtonFragment<CountryPickerViewModel, FragmentCountryPickerBinding>() {

    private val countryPickerViewModel by viewModels<CountryPickerViewModel>()

    private val adapter = CountriesListAdapter(::selectCountry)

    override fun initViewModel() {
        viewModel = countryPickerViewModel
    }

    override fun layoutId(): Int = R.layout.fragment_country_picker

    override fun onViewCreated() {
        viewModel.countries.observe(this) {
            doOnData {
                adapter.submitList(it)
                binding.countriesList.adapter = adapter
            }
            doOnError {
                adapter.submitList(emptyList())
            }
        }
    }

    private fun selectCountry(country: CountryUIModel) {
        // TODO: close the fragment and pass back the information of the selected country
    }
}
