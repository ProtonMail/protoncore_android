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

package me.proton.core.countries.presentation.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.countries.domain.usecase.LoadCountries
import me.proton.core.countries.presentation.entity.CountryUIModel
import me.proton.core.presentation.viewmodel.ProtonViewModel
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

/**
 * View model class used to support the country picker UI.
 */
class CountryPickerViewModel @ViewModelInject constructor(
    loadCountries: LoadCountries
) : ProtonViewModel(), ViewStateStoreScope {

    val countries = ViewStateStore<State>().lock

    sealed class State {
        data class Success(val countries: List<CountryUIModel>) : State()
        data class Error(val message: String?) : State()
    }

    init {
        flow {
            emit(
                State.Success(loadCountries().map {
                    CountryUIModel(it.code, it.callingCode, it.name)
                })
            )
        }.catch {
            countries.post(State.Error(it.message))
        }.onEach {
            countries.post(it)
        }.launchIn(viewModelScope)
    }
}
