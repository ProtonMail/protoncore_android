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

package me.proton.core.configuration.configurator.featureflag.entity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.featureflag.data.api.Feature
import java.util.regex.Pattern

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
