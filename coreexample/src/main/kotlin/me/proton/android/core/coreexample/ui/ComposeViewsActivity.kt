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

package me.proton.android.core.coreexample.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.proton.android.core.coreexample.R
import me.proton.core.compose.component.ProtonOutlinedButton
import me.proton.core.compose.component.ProtonSecondaryButton
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.caption
import me.proton.core.compose.theme.captionHint
import me.proton.core.compose.theme.captionStrong
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.theme.default
import me.proton.core.compose.theme.defaultHint
import me.proton.core.compose.theme.defaultInverted
import me.proton.core.compose.theme.defaultSmall
import me.proton.core.compose.theme.defaultSmallInverted
import me.proton.core.compose.theme.defaultSmallStrong
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.compose.theme.defaultStrong
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.compose.theme.headline
import me.proton.core.compose.theme.headlineHint
import me.proton.core.compose.theme.headlineSmall
import me.proton.core.presentation.ui.ProtonActivity

private val elementPadding = 8.dp

class ComposeViewsActivity : ProtonActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProtonTheme {
                Column {
                    ProtonTopAppBar(modifier = Modifier.fillMaxWidth(), title = { Text("Proton Compose UI") })
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Texts()
                        Spacer(modifier = Modifier.height(16.dp))
                        Buttons()
                    }
                }
            }
        }
    }
}

@Composable
fun Texts() {
    // Headline
    Text(
        text = stringResource(id = R.string.proton_text_headline),
        style = ProtonTheme.typography.headline,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_headline_hint),
        style = ProtonTheme.typography.headlineHint,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_headline_small),
        style = ProtonTheme.typography.headlineSmall,
        modifier = Modifier.padding(top = elementPadding)
    )

    // Default
    Text(
        text = stringResource(id = R.string.proton_text_default),
        style = ProtonTheme.typography.default,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_default_bold),
        style = ProtonTheme.typography.defaultStrong,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_default_weak),
        style = ProtonTheme.typography.defaultWeak,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_default_hint),
        style = ProtonTheme.typography.defaultHint,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_default_inverted),
        style = ProtonTheme.typography.defaultInverted,
        modifier = Modifier
            .padding(top = elementPadding)
            .background(colorResource(id = R.color.interaction_strong))
    )

    // Default small
    Text(
        text = stringResource(id = R.string.proton_text_default_small),
        style = ProtonTheme.typography.defaultSmall,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_default_small_medium),
        style = ProtonTheme.typography.defaultSmallStrong,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_default_small_weak),
        style = ProtonTheme.typography.defaultSmallWeak,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_default_small_inverted),
        style = ProtonTheme.typography.defaultSmallInverted,
        modifier = Modifier
            .padding(top = elementPadding)
            .background(colorResource(id = R.color.interaction_strong))
    )

    // Caption
    Text(
        text = stringResource(id = R.string.proton_text_caption),
        style = ProtonTheme.typography.caption,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_caption_strong),
        style = ProtonTheme.typography.captionStrong,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_caption_weak),
        style = ProtonTheme.typography.captionWeak,
        modifier = Modifier.padding(top = elementPadding)
    )
    Text(
        text = stringResource(id = R.string.proton_text_caption_hint),
        style = ProtonTheme.typography.captionHint,
        modifier = Modifier.padding(top = elementPadding)
    )
}

@Composable
fun Buttons() {
    ProtonSolidButton(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = elementPadding)
    ) {
        Text(stringResource(id = R.string.example_default_button))
    }

    ProtonSolidButton(
        onClick = {},
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = elementPadding)
    ) {
        Text(stringResource(id = R.string.example_default_button))
    }

    ProtonTextButton(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = elementPadding)
    ) {
        Text(stringResource(id = R.string.example_default_button))
    }

    ProtonOutlinedButton(
        onClick = {}, Modifier
            .fillMaxWidth()
            .padding(top = elementPadding)
    ) {
        Text(stringResource(id = R.string.example_default_button))
    }

    ProtonOutlinedButton(
        onClick = {},
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = elementPadding)
    ) {
        Text(stringResource(id = R.string.example_default_button))
    }

    ProtonSolidButton(
        contained = false,
        onClick = {},
        loading = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = elementPadding)
    ) {
        Text(stringResource(id = R.string.example_default_button))
    }

    ProtonTextButton(
        contained = false,
        onClick = {},
        loading = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = elementPadding)
    ) {
        Text(stringResource(id = R.string.example_default_button))
    }

    ProtonSecondaryButton(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = elementPadding)
    ) {
        Text(stringResource(id = R.string.example_default_button))
    }

    ProtonSecondaryButton(
        onClick = {},
        loading = true,
        modifier = Modifier.padding(top = elementPadding)
    ) {
        Text(
            stringResource(id = R.string.example_default_button),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
