/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.configuration.configurator.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.configuration.configurator.BuildConfig
import me.proton.core.configuration.configurator.R
import me.proton.core.configuration.configurator.extension.getProxyToken
import me.proton.core.configuration.configurator.presentation.components.ConfigurationScreen
import me.proton.core.configuration.configurator.presentation.components.FieldActionMap
import me.proton.core.configuration.configurator.presentation.viewModel.ConfigurationScreenViewModel
import me.proton.core.configuration.entity.ConfigContract
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.test.quark.v2.QuarkCommand
import javax.inject.Inject

@AndroidEntryPoint
class ConfigurationActivity : ProtonActivity() {

    @Inject
    lateinit var quark: QuarkCommand

    @Inject
    lateinit var contentResolverConfigManager: ContentResolverConfigManager

    private val basicEnvConfigFields: FieldActionMap = mapOf(
        ConfigContract::host.name to null,
        ConfigContract::proxyToken.name to {
            quark.baseUrl(BuildConfig.PROXY_URL).getProxyToken() ?: error("Could not obtain proxy token")
        },
    )

    private val advancedEnvConfigFields: FieldActionMap = basicEnvConfigFields + mapOf(
        ConfigContract::apiHost.name to null,
        ConfigContract::apiPrefix.name to null,
        ConfigContract::baseUrl.name to null,
        ConfigContract::hv3Host.name to null,
        ConfigContract::hv3Url.name to null,
        ConfigContract::useDefaultPins.name to null,
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            val snackbarHostState = remember { ProtonSnackbarHostState() }
            Box {
                ProtonTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column {
                            ConfigurationScreen(
                                configViewModel = ConfigurationScreenViewModel(
                                    contentResolverConfigManager = contentResolverConfigManager,
                                    configFieldMapper = EnvironmentConfiguration::fromMap,
                                    defaultConfig = EnvironmentConfiguration.fromMap(mapOf())
                                ),
                                basicFields = basicEnvConfigFields,
                                advancedFields = advancedEnvConfigFields,
                                preservedFields = setOf(
                                    ConfigContract::host.name,
                                    ConfigContract::apiPrefix.name,
                                    ConfigContract::proxyToken.name
                                ),
                                snackbarHostState = snackbarHostState,
                                title = stringResource(id = R.string.configuration_title_network_configuration)
                            )
                        }
                    }

                    ProtonSnackbarHost(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        hostState = snackbarHostState
                    )
                }
            }
        }
    }
}
