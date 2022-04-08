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
package me.proton.core.compose.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallInverted
import me.proton.core.compose.theme.defaultSmallStrongInverted

@Composable
fun ProtonErrorMessage(
    errorMessage: String,
    modifier: Modifier = Modifier,
    elevation: Dp = Elevation
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinHeight),
        backgroundColor = ProtonTheme.colors.notificationError,
        shape = ProtonTheme.shapes.medium,
        elevation = elevation

    ) {
        Text(
            text = errorMessage,
            style = ProtonTheme.typography.defaultSmallInverted,
            modifier = Modifier
                .padding(
                    start = MessageHorizontalPadding,
                    top = MessageVerticalPadding,
                    end = Padding,
                    bottom = MessageHorizontalPadding
                ),
        )
    }
}

@Composable
fun ProtonErrorMessageWithAction(
    errorMessage: String,
    modifier: Modifier = Modifier,
    elevation: Dp = Elevation,
    action: String,
    onAction: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinHeight),
        backgroundColor = ProtonTheme.colors.notificationError,
        shape = ProtonTheme.shapes.medium,
        elevation = elevation,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Padding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = errorMessage,
                style = ProtonTheme.typography.defaultSmallInverted,
                modifier = Modifier
                    .padding(horizontal = MessageWithActionHorizontalPadding, vertical = MessageVerticalPadding)
                    .weight(1f),
            )
            ProtonTextButton(
                modifier = Modifier.padding(horizontal = ActionHorizontalPadding, vertical = ActionVerticalPadding),
                onClick = { onAction() }
            ) {
                Text(
                    text = action,
                    style = ProtonTheme.typography.defaultSmallStrongInverted,
                )
            }
        }

    }
}

@Preview
@Composable
fun PreviewProtonErrorMessage() {
    ProtonErrorMessage(errorMessage = "Some error message to display.")
}

@Preview
@Composable
fun PreviewProtonErrorMessageMultiline() {
    val message =
        """Some error message to display. This is a longer message so the text gets wrapped.
We can also
add some line breaks."""
    ProtonErrorMessage(errorMessage = message)
}

@Preview
@Composable
fun PreviewProtonErrorMessageWithAction() {
    ProtonErrorMessageWithAction(errorMessage = "Some error message to display.", action = "Close") {}
}

private val Padding = 8.dp
private val MinHeight = 48.dp
private val Elevation = 6.dp
private val MessageHorizontalPadding = 16.dp
private val MessageWithActionHorizontalPadding = 8.dp
private val MessageVerticalPadding = 14.dp
private val ActionHorizontalPadding = 12.dp
private val ActionVerticalPadding = 8.dp
val ErrorPadding = 16.dp
