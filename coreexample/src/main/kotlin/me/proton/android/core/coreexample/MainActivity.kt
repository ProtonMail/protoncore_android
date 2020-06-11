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

package me.proton.android.core.coreexample

import me.proton.android.core.coreexample.databinding.ActivityMainBinding
import me.proton.android.core.coreexample.viewmodel.MainViewModel
import me.proton.android.core.presentation.ui.ContentLayout
import me.proton.android.core.presentation.ui.ProtonActivity
import me.proton.core.humanverification.presentation.utils.showHumanVerification

@ContentLayout(R.layout.activity_main)
class MainActivity : ProtonActivity<ActivityMainBinding, MainViewModel>() {

    override fun initViewModel() {
        binding.humanVerification.setOnClickListener {
            supportFragmentManager.showHumanVerification(largeLayout = false, containerId =  binding.fragmentContainer.id)
        }
    }
}
