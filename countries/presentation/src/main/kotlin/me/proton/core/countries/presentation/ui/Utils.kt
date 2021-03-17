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

package me.proton.core.countries.presentation.ui

import androidx.fragment.app.FragmentManager
import me.proton.core.presentation.utils.inTransaction

private const val TAG_PROTON_COUNTRY_PICKER = "proton_country_picker"

fun FragmentManager.showCountryPicker(withCallingCode: Boolean = true) {
    val countryPickerFragment = CountryPickerFragment(withCallingCode)
    inTransaction {
        setCustomAnimations(0, 0)
        add(countryPickerFragment, TAG_PROTON_COUNTRY_PICKER)
    }
}

internal fun CountryPickerFragment.removeCountryPicker() {
    parentFragmentManager.inTransaction {
        remove(this@removeCountryPicker)
    }
}
