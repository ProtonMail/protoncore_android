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

package me.proton.core.auth.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.alert.registerConfirmPasswordResultLauncher
import me.proton.core.auth.presentation.databinding.ActivityConfirmPasswordBinding
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordInput
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordResult
import me.proton.core.presentation.ui.ProtonViewBindingActivity

@AndroidEntryPoint
class ConfirmPasswordActivity :
    ProtonViewBindingActivity<ActivityConfirmPasswordBinding>(ActivityConfirmPasswordBinding::inflate) {

    private val input: ConfirmPasswordInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.apply {
            registerConfirmPasswordResultLauncher(
                this@ConfirmPasswordActivity
            ) { result ->
                val intent = Intent().putExtra(
                    ARG_RESULT,
                    ConfirmPasswordResult(result?.obtained ?: false)
                )
                setResult(Activity.RESULT_OK, intent)
                finish()
            }.show(input)
        }
    }

    companion object {
        const val ARG_INPUT = "arg.confirmPasswordInput"
        const val ARG_RESULT = "arg.confirmPasswordResult"
    }
}
