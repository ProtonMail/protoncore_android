/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.usersettings.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.compose.theme.AppTheme
import me.proton.core.usersettings.presentation.R
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.openBrowserLink
import me.proton.core.usersettings.presentation.compose.SecurityKeysRoutes
import me.proton.core.usersettings.presentation.compose.SecurityKeysRoutes.addSecurityKeysScreen
import me.proton.core.usersettings.presentation.entity.SettingsInput
import javax.inject.Inject

@AndroidEntryPoint
class SecurityKeysActivity : ProtonActivity() {

    @Inject
    lateinit var appTheme: AppTheme

    private val input: SettingsInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            appTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = SecurityKeysRoutes.Route.SecurityKeys.get(input.user)
                ) {
                    addSecurityKeysScreen(
                        userId = input.user,
                        onAddSecurityKeyClicked = {
                            openBrowserLink(getString(R.string.add_security_key_link))
                        },
                        onManageSecurityKeysClicked = {
                            openBrowserLink(getString(R.string.manage_security_keys_link))
                        },
                        onClose = { finish() }
                    )
                }
            }
        }
    }

    companion object {

        const val ARG_INPUT = "arg.securityKeysInput"

    }
}