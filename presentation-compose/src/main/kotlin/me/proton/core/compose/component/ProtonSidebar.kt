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

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionHint
import me.proton.core.presentation.compose.R

@Composable
fun ProtonSidebar(
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    content: (@Composable @ExtensionFunctionType ColumnScope.() -> Unit),
) {
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val sidebarColors = requireNotNull(ProtonTheme.colors.sidebarColors)

    ProtonTheme(colors = sidebarColors) {
        Surface(
            color = sidebarColors.backgroundNorm,
            contentColor = sidebarColors.textNorm,
            modifier = modifier.fillMaxSize()
        ) {
            val state = rememberScrollState()

            Column(
                modifier = Modifier.verticalScroll(enabled = true, state = state),
                content = content
            )
        }
    }
}

@Composable
fun ProtonSidebarSettingsItem(
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    onClick: () -> Unit = {},
) {
    ProtonSidebarItem(
        text = R.string.presentation_menu_item_title_settings,
        icon = R.drawable.ic_cog_wheel,
        modifier = modifier,
        onClick = onClick,
        isClickable = isClickable,
        isSelected = false
    )
}

@Composable
fun ProtonSidebarSubscriptionItem(
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    onClick: () -> Unit = {},
) {
    ProtonSidebarItem(
        text = R.string.presentation_menu_item_title_subscription,
        icon = R.drawable.ic_pencil,
        modifier = modifier,
        onClick = onClick,
        isClickable = isClickable,
        isSelected = false
    )
}

@Composable
fun ProtonSidebarReportBugItem(
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    onClick: () -> Unit = {},
) {
    ProtonSidebarItem(
        text = R.string.presentation_menu_item_title_report_a_bug,
        icon = R.drawable.ic_bug,
        modifier = modifier,
        onClick = onClick,
        isClickable = isClickable,
        isSelected = false
    )
}

@Composable
fun ProtonSidebarSignOutItem(
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    onClick: () -> Unit = {},
) {
    ProtonSidebarItem(
        text = R.string.presentation_menu_item_title_sign_out,
        icon = R.drawable.ic_sign_out,
        modifier = modifier,
        onClick = onClick,
        isClickable = isClickable,
        isSelected = false
    )
}

@Composable
fun ProtonSidebarItem(
    @DrawableRes icon: Int,
    @StringRes text: Int,
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    isSelected: Boolean = false,
    textColor: Color = Color.Unspecified,
    iconTint: Color = ProtonTheme.colors.iconHint,
    count: Int? = null,
    onClick: () -> Unit = {},
) {
    ProtonListItem(
        icon = icon,
        text = text,
        modifier = modifier,
        onClick = onClick,
        isClickable = isClickable,
        isSelected = isSelected,
        textColor = textColor,
        iconTint = iconTint,
        count = count
    )
}

@Composable
fun ProtonSidebarItem(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    isSelected: Boolean = false,
    textColor: Color = Color.Unspecified,
    iconTint: Color = ProtonTheme.colors.iconHint,
    count: Int? = null,
    onClick: () -> Unit = {},
) {
    ProtonListItem(
        icon = icon,
        text = text,
        modifier = modifier,
        onClick = onClick,
        isClickable = isClickable,
        isSelected = isSelected,
        textColor = textColor,
        iconTint = iconTint,
        count = count
    )
}

@Composable
fun ProtonSidebarItem(
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    content: (@Composable @ExtensionFunctionType RowScope.() -> Unit),
) {
    ProtonListItem(
        modifier = modifier,
        onClick = onClick,
        isClickable = isClickable,
        isSelected = isSelected,
        content = content
    )
}

@Composable
fun ProtonSidebarAppVersionItem(
    name: String,
    version: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(ProtonDimens.MediumSpacing),
        text = "$name $version",
        textAlign = TextAlign.Center,
        style = ProtonTheme.typography.captionHint,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview(
    name = "Sidebar in light mode",
    uiMode = UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Sidebar in dark mode",
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
fun PreviewProtonSidebar() {
    ProtonTheme {
        ProtonSidebar(
            drawerState = DrawerState(DrawerValue.Open) { true },
        ) {
            ProtonSidebarItem { Text(text = "Inbox") }
            ProtonSidebarItem { Text(text = "Drafts") }
            ProtonSidebarItem { Text(text = "Sent") }
            ProtonSidebarItem(isSelected = true) { Text(text = "Trash (active)") }
            ProtonSidebarItem { Text(text = "All mail") }

            Divider()

            ProtonSidebarItem { Text(text = "More", color = ProtonTheme.colors.textHint) }
            ProtonSidebarSettingsItem()
            ProtonSidebarSubscriptionItem()
            ProtonSidebarReportBugItem()
            ProtonSidebarSignOutItem()

            ProtonSidebarAppVersionItem(name = "App Name", version = "0.0.7")
        }
    }
}
