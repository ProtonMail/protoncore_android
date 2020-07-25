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

package me.proton.core.humanverification.presentation.viewmodel.verification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.proton.android.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.humanverification.domain.usecase.LoadCountries
import me.proton.core.humanverification.presentation.entity.CountryUIModel
import studio.forface.viewstatestore.ViewState
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View model class used to support the country picker UI.
 *
 * @author Dino Kadrikj.
 */
class CountryPickerViewModel @ViewModelInject constructor(
    private val loadCountries: LoadCountries
) : ProtonViewModel(), ViewStateStoreScope {

    val countries = ViewStateStore<List<CountryUIModel>>(ViewState.Loading).lock

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadCountries().collect { countryList ->
                countries.postData(countryList.map {
                    CountryUIModel(it.code, it.callingCode, it.name)
                }, true)
            }
        }
    }
}
