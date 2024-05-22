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

package me.proton.core.configuration.configurator.presentation.components;

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.R
import me.proton.core.configuration.configurator.featureflag.data.api.Feature
import me.proton.core.configuration.configurator.presentation.viewModel.FeatureFlagsViewModel
import java.util.regex.Pattern

enum class ConfigType(val displayName: String) {
    Calendar("Calendar"),
    Drive("Drive"),
    Mail("Mail"),
    Pass("Pass"),
    VPN("VPN"),
    Wallet("Wallet"),
    Account("Account"),
    Payments("Payments"),
    Common("Common")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val featureFlagsViewModel = FeatureFlagsViewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            ConfigButtons(navController)
        }
        composable("featureFlags/{configType}") { backStackEntry ->
            val project = backStackEntry.arguments?.getString("configType") ?: "Default"
            FeatureFlagsScreen(featureFlagsViewModel, project, navController)
        }
    }
}

@Composable
fun ConfigButtons(navController: NavController, canNavigateBack: Boolean = false) {
    Column {
        ProtonTopAppBar(
            title = { Text(stringResource(R.string.feature_flags_title)) },
            navigationIcon = if (canNavigateBack) {
                { BackButton(navController) }
            } else {
                null
            }
        )
        Spacer(modifier = Modifier.height(ProtonDimens.SmallSpacing))
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(ProtonDimens.DefaultSpacing)) {
                ConfigType.entries.forEach { config ->
                    Button(
                        onClick = { navigateWithConfig(navController, config) },
                        modifier = Modifier.padding(vertical = ProtonDimens.SmallSpacing)
                    ) {
                        Text(text = config.displayName)
                    }
                }
            }
        }
    }
}


@Composable
fun BackButton(navController: NavController) {
    IconButton(onClick = { navController.navigateUp() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}


fun navigateWithConfig(navController: NavController, config: ConfigType) {
    // Navigate to the specific screen with the configuration type as a parameter
    navController.navigate("featureFlags/${config.name}")
}

@Composable
fun FeatureFlagsScreen(featureFlagsViewModel: FeatureFlagsViewModel, project: String, navController: NavController) {
    val context = LocalContext.current
    // Trigger loading feature flags only when the project changes
    LaunchedEffect(project) {
        featureFlagsViewModel.loadFeatureFlagsByProject(project.lowercase())
    }
    val featureFlags = featureFlagsViewModel.featureFlags.collectAsState()
    Column {
        ProtonTopAppBar(
            title = { Text(project) },
            navigationIcon = { BackButton(navController) }
        )
        Surface(
            modifier = Modifier
                .padding(horizontal = ProtonDimens.DefaultSpacing, vertical = ProtonDimens.SmallSpacing)
                .fillMaxWidth(),
            color = Color.LightGray.copy(alpha = 0.1f)
        ) {
            Column {
                Spacer(modifier = Modifier.height(ProtonDimens.SmallSpacing))
                ProtonSolidButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://unleash.protontech.ch/"))
                        context.startActivity(intent)
                    }
                ) {
                    Text(stringResource(id = R.string.feature_flags_manage))
                }
                Spacer(modifier = Modifier.height(ProtonDimens.DefaultSpacing))
                FeatureFlagList(featureFlags.value)
            }
        }
    }
}


@Composable
fun FeatureFlagList(featureFlags: List<Feature>) {
    for (feature in featureFlags) {
        FeatureFlagItem(feature = feature)
        ItemDivider()
    }
}

@Composable
fun ItemDivider() {
    Divider(modifier = Modifier.height(1.dp), color = Color.LightGray)
}

@Composable
fun FeatureFlagItem(feature: Feature) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ProtonDimens.DefaultSpacing)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ProtonDimens.SmallSpacing)
        ) {
            Text(
                text = feature.name ?: "",
                style = TextStyle(
                    fontWeight = FontWeight.Medium, fontFamily = FontFamily.SansSerif
                ),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = feature.isEnabled100(),
                onCheckedChange = null, // To make switch not interactive
                enabled = false
            )
        }
        if (!feature.isEnabled100()) {
            if (feature.strategies.size == 1) {
                val rolloutPercentage = feature.rolloutPercentage() ?: 0
                RolloutProgressView(rollout = rolloutPercentage)
            } else {
                Text(
                    text = highlightedText(from = feature.strategiesDescription(), highlight = "atlas-"),
                    style = LocalTextStyle.current.copy(fontSize = 12.sp)
                )
            }
        }
    }
}


@Composable
fun highlightedText(from: String, highlight: String): AnnotatedString {
    return buildAnnotatedString {
        val pattern = Pattern.compile("$highlight[^\\n]*")
        val matcher = pattern.matcher(from)

        var lastEnd = 0
        while (matcher.find()) {
            append(from.substring(lastEnd, matcher.start()))
            withStyle(style = SpanStyle(color = Color(0xFFFFA500))) { // Orange color
                append(from.substring(matcher.start(), matcher.end()))
            }
            lastEnd = matcher.end()
        }
        append(from.substring(lastEnd))
    }
}

@Composable
fun RolloutProgressView(rollout: Int) {
    Column {
        Text("Rollout: $rollout%", modifier = Modifier.padding(bottom = ProtonDimens.ExtraSmallSpacing))
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(ProtonDimens.CounterIconSize)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = rollout / 100f)
                    .height(ProtonDimens.CounterIconSize)
                    .background(Color.Blue, RoundedCornerShape(ProtonDimens.DefaultCornerRadius))
            )
        }
    }
}
