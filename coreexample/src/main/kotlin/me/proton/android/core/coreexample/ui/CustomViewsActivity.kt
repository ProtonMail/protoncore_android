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

package me.proton.android.core.coreexample.ui

import android.os.Bundle
import android.text.InputType
import me.proton.android.core.coreexample.R
import me.proton.android.core.coreexample.databinding.ActivityCustomViewsBinding
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.ui.view.ProtonProgressButton
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.forEach

/**
 * Demonstrates the custom views from presentation module.
 * @author Dino Kadrikj.
 */
class CustomViewsActivity : ProtonActivity<ActivityCustomViewsBinding>() {
    override fun layoutId(): Int = R.layout.activity_custom_views


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        forEach(binding.loadingButton, binding.loadingTextOnlyButton) {
            it.onClick {
                when (it.currentState) {
                    ProtonProgressButton.State.LOADING -> it.setIdle()
                    ProtonProgressButton.State.IDLE -> it.setLoading()
                }
            }
        }

        binding.inputExample.inputType = InputType.TYPE_CLASS_PHONE
        binding.errorExample.setInputError("Error in your text")
    }
}
