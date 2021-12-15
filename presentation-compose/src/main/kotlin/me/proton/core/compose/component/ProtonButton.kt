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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun ProtonSolidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    contained: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    ProtonButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonDefaults.MinHeight),
        enabled = enabled,
        loading = loading,
        contained = contained,
        interactionSource = interactionSource,
        elevation = ButtonDefaults.protonElevation(),
        shape = ProtonTheme.shapes.small,
        border = null,
        colors = ButtonDefaults.protonButtonColors(loading),
        contentPadding = ButtonDefaults.ContentPadding,
        content = content,
    )
}

@Composable
fun ButtonDefaults.protonElevation() = elevation()

@Composable
fun ButtonDefaults.protonButtonColors(
    loading: Boolean = false,
    backgroundColor: Color = ProtonTheme.colors.interactionNorm,
    contentColor: Color = Color.White,
    disabledBackgroundColor: Color = if (loading) {
        ProtonTheme.colors.interactionPressed
    } else {
        ProtonTheme.colors.brandLighten40
    },
    disabledContentColor: Color = if (loading) {
        Color.White
    } else {
        Color.White.copy(alpha = 0.5f)
    },
): ButtonColors = buttonColors(
    backgroundColor = backgroundColor,
    contentColor = contentColor,
    disabledBackgroundColor = disabledBackgroundColor,
    disabledContentColor = disabledContentColor,
)

@Composable
fun ProtonOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    contained: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    ProtonButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonDefaults.MinHeight),
        enabled = enabled,
        loading = loading,
        contained = contained,
        interactionSource = interactionSource,
        elevation = null,
        shape = ProtonTheme.shapes.small,
        border = ButtonDefaults.protonOutlinedBorder(enabled, loading),
        colors = ButtonDefaults.protonOutlinedButtonColors(loading),
        contentPadding = ButtonDefaults.ContentPadding,
        content = content,
    )
}

@Composable
fun ButtonDefaults.protonOutlinedBorder(
    enabled: Boolean = true,
    loading: Boolean = false,
) = BorderStroke(
    OutlinedBorderSize,
    when {
        loading -> ProtonTheme.colors.interactionPressed
        !enabled -> ProtonTheme.colors.interactionDisabled
        else -> ProtonTheme.colors.brandNorm
    },
)

@Composable
fun ButtonDefaults.protonOutlinedButtonColors(
    loading: Boolean = false,
    backgroundColor: Color = ProtonTheme.colors.backgroundNorm,
    contentColor: Color = ProtonTheme.colors.interactionNorm,
    disabledBackgroundColor: Color = if (loading) {
        ProtonTheme.colors.backgroundSecondary
    } else {
        ProtonTheme.colors.backgroundNorm
    },
    disabledContentColor: Color = if (loading) {
        ProtonTheme.colors.interactionPressed
    } else {
        ProtonTheme.colors.interactionDisabled
    },
): ButtonColors = buttonColors(
    backgroundColor = backgroundColor,
    contentColor = contentColor,
    disabledBackgroundColor = disabledBackgroundColor,
    disabledContentColor = disabledContentColor,
)

@Composable
fun ProtonTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    contained: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    ProtonButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonDefaults.MinHeight),
        enabled = enabled,
        loading = loading,
        contained = contained,
        interactionSource = interactionSource,
        elevation = null,
        shape = ProtonTheme.shapes.small,
        border = null,
        colors = ButtonDefaults.protonTextButtonColors(loading),
        contentPadding = ButtonDefaults.TextButtonContentPadding,
        content = content,
    )
}

@Composable
fun ButtonDefaults.protonTextButtonColors(
    loading: Boolean = false,
    backgroundColor: Color = if (loading) {
        ProtonTheme.colors.backgroundSecondary
    } else {
        Color.Transparent
    },
    contentColor: Color = ProtonTheme.colors.interactionNorm,
    disabledBackgroundColor: Color = if (loading) {
        ProtonTheme.colors.backgroundSecondary
    } else {
        Color.Transparent
    },
    disabledContentColor: Color = if (loading) {
        ProtonTheme.colors.interactionPressed
    } else {
        ProtonTheme.colors.textDisabled
    },
): ButtonColors = buttonColors(
    backgroundColor = backgroundColor,
    contentColor = contentColor,
    disabledBackgroundColor = disabledBackgroundColor,
    disabledContentColor = disabledContentColor,
)

@Composable
fun ProtonSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    ProtonButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        interactionSource = interactionSource,
        elevation = null,
        shape = ProtonTheme.shapes.small,
        border = null,
        colors = ButtonDefaults.protonSecondaryButtonColors(loading),
        contentPadding = ButtonDefaults.TextButtonContentPadding,
        content = content,
    )
}

@Composable
fun ButtonDefaults.protonSecondaryButtonColors(
    loading: Boolean = false,
    backgroundColor: Color = ProtonTheme.colors.interactionWeakNorm,
    contentColor: Color = ProtonTheme.colors.textNorm,
    disabledBackgroundColor: Color = if (loading) {
        ProtonTheme.colors.interactionWeakPressed
    } else {
        ProtonTheme.colors.interactionWeakDisabled
    },
    disabledContentColor: Color = if (loading) {
        ProtonTheme.colors.textNorm
    } else {
        ProtonTheme.colors.textDisabled
    },
): ButtonColors = buttonColors(
    backgroundColor = backgroundColor,
    contentColor = contentColor,
    disabledContentColor = disabledContentColor,
    disabledBackgroundColor = disabledBackgroundColor,
)

@Suppress("LongParameterList")
@Composable
fun ProtonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    contained: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation?,
    shape: Shape,
    border: BorderStroke?,
    colors: ButtonColors,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonDefaults.MinHeight),
        enabled = !loading && enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        shape = shape,
        border = border,
        colors = colors,
        contentPadding = contentPadding,
    ) {
        ProtonButtonContent(
            loading = loading,
            contained = contained,
            content = content,
            progressColor = colors.contentColor(enabled = false).value,
        )
    }
}

@Composable
private fun ProtonButtonContent(
    loading: Boolean = false,
    contained: Boolean = true,
    progressColor: Color,
    content: @Composable () -> Unit,
) {
    if (!contained) {
        Box(Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                content()
            }
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(LoadingIndicatorSize)
                        .align(Alignment.CenterEnd),
                    color = progressColor,
                    strokeWidth = LoadingIndicatorStroke,
                )
            }
        }
    } else {
        content()
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(horizontal = ProtonDimens.DefaultSpacing)
                    .size(LoadingIndicatorSize),
                color = progressColor,
                strokeWidth = LoadingIndicatorStroke,
            )
        }
    }
}

private val LoadingIndicatorSize = 14.dp
private val LoadingIndicatorStroke = 1.dp

@Preview(
    widthDp = 640,
    showBackground = true,
    backgroundColor = android.graphics.Color.WHITE.toLong()
)
@Suppress("unused")
@Composable
private fun PreviewProtonSolidButton() {
    PreviewHelper { enabled, contained, loading ->
        ProtonSolidButton(
            enabled = enabled,
            contained = contained,
            loading = loading,
            onClick = { }
        ) {
            Text(text = "Button")
        }
    }
}

@Preview(
    widthDp = 640,
    showBackground = true,
    backgroundColor = android.graphics.Color.WHITE.toLong()
)
@Suppress("unused")
@Composable
private fun PreviewProtonOutlinedButton() {
    PreviewHelper { enabled, contained, loading ->
        ProtonOutlinedButton(
            enabled = enabled,
            contained = contained,
            loading = loading,
            onClick = { }
        ) {
            Text(text = "Button")
        }
    }
}

@Preview(
    widthDp = 640,
    showBackground = true,
    backgroundColor = android.graphics.Color.WHITE.toLong()
)
@Suppress("unused")
@Composable
private fun PreviewProtonTextButton() {
    PreviewHelper { enabled, contained, loading ->
        ProtonTextButton(
            enabled = enabled,
            contained = contained,
            loading = loading,
            onClick = { }
        ) {
            Text(text = "Button")
        }
    }
}

@Preview(
    widthDp = 640,
    showBackground = true,
    backgroundColor = android.graphics.Color.WHITE.toLong()
)
@Suppress("unused")
@Composable
private fun PreviewProtonSecondaryButton() {
    PreviewHelper { enabled, _, loading ->
        ProtonSecondaryButton(
            enabled = enabled,
            loading = loading,
            onClick = { }
        ) {
            Text(text = "Button")
        }
    }
}

@Composable
private inline fun PreviewHelper(
    crossinline button: @Composable (enabled: Boolean, contained: Boolean, loading: Boolean) -> Unit,
) {
    ProtonTheme {
        Column(Modifier.padding(10.dp)) {
            PreviewRowHelper(
                enabled = true,
                loading = false,
                button,
            )
            PreviewRowHelper(
                enabled = false,
                loading = false,
                button,
            )
            PreviewRowHelper(
                enabled = true,
                loading = true,
                button,
            )
        }
    }
}

@Composable
private inline fun PreviewRowHelper(
    enabled: Boolean,
    loading: Boolean,
    button: @Composable (enabled: Boolean, contained: Boolean, loading: Boolean) -> Unit,
) {
    Row(Modifier.padding(bottom = 20.dp)) {
        Box(Modifier.width(320.dp)) {
            button(enabled, false, loading)
        }
        Box(Modifier.width(320.dp).padding(start = 20.dp)) {
            button(enabled, true, loading)
        }
    }
}
