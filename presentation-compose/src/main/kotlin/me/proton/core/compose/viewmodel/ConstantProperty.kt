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

package me.proton.core.compose.viewmodel

import androidx.lifecycle.ViewModel

/**
 * Defines the default [stopTimeoutMillis] used by viewModel when using [stateIn] and [SharingStarted.WhileSubscribed].
 * ```
 * .stateIn(
 *     scope = viewModelScope,
 *     started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
 *     initialValue = initialValue
 * )
 * ```
 */
val ViewModel.stopTimeoutMillis: Long
    get() = 5000L
