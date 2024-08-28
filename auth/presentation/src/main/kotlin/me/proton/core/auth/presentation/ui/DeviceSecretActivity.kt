/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and ProtonCore.
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
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.compose.DeviceSecretAction
import me.proton.core.auth.presentation.compose.DeviceSecretScreen
import me.proton.core.auth.presentation.compose.DeviceSecretViewModel
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback

@AndroidEntryPoint
public class DeviceSecretActivity : ProtonActivity() {

    private val viewModel by viewModels<DeviceSecretViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback { viewModel.submit(DeviceSecretAction.Close) }

        setContent {
            ProtonTheme {
                DeviceSecretScreen(
                    onClose = { onClose() },
                )
            }
        }
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onSuccess() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val ARG_INPUT = DeviceSecretScreen.KEY_USERID
        const val ARG_RESULT = "arg.result"
    }
}
