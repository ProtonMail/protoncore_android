package me.proton.core.humanverification.presentation.viewmodel.verification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.proton.android.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.humanverification.domain.usecase.LoadCountriesUseCase
import me.proton.core.humanverification.presentation.entity.CountryUIModel
import me.proton.core.util.kotlin.map
import studio.forface.viewstatestore.ViewState
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.post

/**
 * Created by dinokadrikj on 6/17/20.
 */
class CountryPickerViewModel @ViewModelInject constructor(
    private val loadCountries: LoadCountriesUseCase
) : ProtonViewModel() {

    val countries = ViewStateStore<List<CountryUIModel>>(ViewState.Loading)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadCountries().collect { countryList ->
                countries.post(countryList.map {
                    CountryUIModel(it.code, it.callingCode as String, it.name)
                }, true)
            }
        }
    }
}
