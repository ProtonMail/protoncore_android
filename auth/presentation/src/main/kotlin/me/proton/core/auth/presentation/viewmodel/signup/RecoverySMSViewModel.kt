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

package me.proton.core.auth.presentation.viewmodel.signup

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.country.domain.usecase.DefaultCountry
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.presentation.viewmodel.ViewModelResult
import javax.inject.Inject

@HiltViewModel
internal class RecoverySMSViewModel @Inject constructor(
    private val defaultCountry: DefaultCountry
) : ProtonViewModel() {

    private val _countryCallingCode = MutableStateFlow<ViewModelResult<Int>>(ViewModelResult.None)

    val countryCallingCode = _countryCallingCode.asStateFlow()

    /**
     * Returns country calling code and later to display it as a suggestion in the SMS verification UI.
     */
    fun getCountryCallingCode() = flow {
        emit(ViewModelResult.Processing)
        val code = defaultCountry()?.callingCode
        code?.let {
            emit(ViewModelResult.Success(it))
        } ?: run {
            emit(ViewModelResult.Error(null))
        }
    }.catch { error ->
        emit(ViewModelResult.Error(error))
    }.onEach {
        _countryCallingCode.tryEmit(it)
    }.launchIn(viewModelScope)
}
