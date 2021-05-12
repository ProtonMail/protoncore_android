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

package me.proton.core.country.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.country.domain.usecase.LoadCountries
import me.proton.core.country.presentation.entity.CountryUIModel
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

/**
 * View model class used to support the country picker UI.
 */
@HiltViewModel
class CountryPickerViewModel @Inject constructor(
    loadCountries: LoadCountries
) : ProtonViewModel() {

    private val _countries = MutableStateFlow<State>(State.Idle)

    val countries = _countries.asStateFlow()

    sealed class State {
        object Idle : State()
        data class Success(val countries: List<CountryUIModel>) : State()
        data class Error(val message: String?) : State()
    }

    init {
        flow {
            emit(
                State.Success(
                    loadCountries().map {
                        CountryUIModel(it.code, it.callingCode, it.name)
                    }
                )
            )
        }.catch {
            _countries.tryEmit(State.Error(it.message))
        }.onEach {
            _countries.tryEmit(it)
        }.launchIn(viewModelScope)
    }
}
