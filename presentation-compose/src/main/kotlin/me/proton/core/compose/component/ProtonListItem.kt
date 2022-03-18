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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Horizontal
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.compose.R

/**
 * A basic, highly customizable list item to be used in lists that requires particular layouts.
 * By default, this list item is full-width, vertically centered and horizontally aligned at start.
 * @param horizontalAlignment allows changing the horizontal alignment
 *
 * A sample use case where this was used is [ProtonSettingsToggleItem] where we
 * used an horizontal "Space between" to layout a switch
 */
@Composable
fun ProtonRawListItem(
    modifier: Modifier = Modifier,
    horizontalArrangement: Horizontal = Arrangement.Start,
    content: @Composable (RowScope.() -> Unit),
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
        horizontalArrangement = horizontalArrangement
    )
}

@Composable
fun ProtonListItem(
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    content: (@Composable @ExtensionFunctionType RowScope.() -> Unit),
) {
    ProtonRawListItem(
        modifier = modifier
            .fillMaxWidth()
            .background(color = if (isSelected) ProtonTheme.colors.interactionPressed else Color.Transparent)
            .height(height = ProtonDimens.ListItemHeight)
            .clickable(enabled = isClickable, onClick = onClick)
            .padding(horizontal = ProtonDimens.DefaultSpacing),
        content = content
    )
}

@Composable
fun ProtonListItem(
    icon: @Composable @ExtensionFunctionType RowScope.(Modifier) -> Unit,
    text: @Composable @ExtensionFunctionType RowScope.(Modifier) -> Unit,
    count: @Composable @ExtensionFunctionType RowScope.(Modifier) -> Unit,
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    ProtonListItem(
        modifier = modifier,
        onClick = onClick,
        isClickable = isClickable,
        isSelected = isSelected,
    ) {
        icon(Modifier.padding(end = ProtonDimens.DefaultSpacing))
        text(Modifier.weight(1f, fill = true))
        count(Modifier.weight(1f))
    }
}

@Composable
fun ProtonListItem(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    isSelected: Boolean = false,
    textColor: Color = Color.Unspecified,
    iconTint: Color = LocalContentColor.current,
    count: Int? = null,
    onClick: () -> Unit = {},
) {
    ProtonListItem(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = text },
        onClick = onClick,
        isClickable = isClickable,
        isSelected = isSelected,
        icon = { iconModifier ->
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = iconModifier,
                tint = iconTint
            )
        },
        text = { titleModifier ->
            Text(
                text = text,
                modifier = titleModifier,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        count = {
            if (count != null) {
                Text(
                    modifier = Modifier
                        .defaultMinSize(ProtonDimens.CounterIconSize)
                        .background(color = ProtonTheme.colors.interactionNorm, shape = CircleShape),
                    text = "$count",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = ProtonTheme.typography.captionRegular,
                )
            }
        }
    )
}

@Composable
fun ProtonListItem(
    @DrawableRes icon: Int,
    @StringRes text: Int,
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    isSelected: Boolean = false,
    textColor: Color = Color.Unspecified,
    iconTint: Color = LocalContentColor.current,
    count: Int? = null,
    onClick: () -> Unit = {}
) = ProtonListItem(
    icon = painterResource(icon),
    text = stringResource(text),
    onClick = onClick,
    isClickable = isClickable,
    isSelected = isSelected,
    modifier = modifier,
    textColor = textColor,
    iconTint = iconTint,
    count = count
)

@Preview(
    showBackground = true,
    backgroundColor = android.graphics.Color.WHITE.toLong()
)
@Composable
fun PreviewProtonListItem() {
    ProtonListItem(
        icon = R.drawable.ic_proton_arrow_out_from_rectangle,
        text = R.string.presentation_menu_item_title_sign_out,
    )
}

@Preview(
    showBackground = true,
    backgroundColor = android.graphics.Color.BLACK.toLong()
)
@Composable
fun PreviewProtonListItemDark() {
    ProtonTheme(isDark = true) {
        PreviewProtonListItem()
    }
}

@Preview(
    showBackground = true,
    backgroundColor = android.graphics.Color.WHITE.toLong()
)
@Composable
fun PreviewProtonListItemSelected() {
    ProtonListItem(
        icon = R.drawable.ic_proton_arrow_out_from_rectangle,
        text = R.string.presentation_menu_item_title_sign_out,
        isSelected = true
    )
}

@Preview(
    showBackground = true,
    backgroundColor = android.graphics.Color.WHITE.toLong()
)
@Composable
fun PreviewProtonListItemCount() {
    ProtonListItem(
        icon = R.drawable.ic_proton_arrow_out_from_rectangle,
        text = R.string.presentation_menu_item_title_sign_out,
        count = 1
    )
}
