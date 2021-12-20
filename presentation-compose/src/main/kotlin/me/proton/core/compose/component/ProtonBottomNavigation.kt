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
import androidx.compose.foundation.layout.Box
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonColors
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.compose.R

data class NavigationTab(
    val title: @Composable () -> Unit,
    val icon: @Composable () -> Unit,
) {

    constructor(
        @StringRes title: Int,
        @DrawableRes icon: Int,
    ) : this(
        title = {
            Text(stringResource(id = title))
        },
        icon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = stringResource(id = title)
            )
        }
    )

    constructor(
        @StringRes title: Int,
        icon: @Composable () -> Unit,
    ) : this(
        title = {
            Text(stringResource(id = title))
        },
        icon = icon
    )

    constructor(
        @StringRes title: Int,
        imageVector: ImageVector,
    ) : this(
        title = {
            Text(stringResource(id = title))
        },
        icon = {
            Icon(
                imageVector = imageVector,
                contentDescription = stringResource(id = title)
            )
        }
    )
}

@Composable
fun ProtonBottomNavigation(
    tabs: List<NavigationTab>,
    modifier: Modifier = Modifier,
    initialSelectedTabIndex: Int = 0,
    onSelectedTabIndex: (Int) -> Unit,
) {
    var selectedIndex by remember { mutableStateOf(initialSelectedTabIndex) }

    fun onItemClick(index: Int) {
        selectedIndex = index
        onSelectedTabIndex(index)
    }

    Box(modifier) {
        BottomNavigation(backgroundColor = ProtonTheme.colors.backgroundNorm) {
            tabs.forEachIndexed { index, tab ->
                BottomNavigationItem(
                    icon = tab.icon,
                    label = tab.title,
                    selected = selectedIndex == index,
                    onClick = { onItemClick(index) },
                    selectedContentColor = ProtonTheme.colors.interactionNorm,
                    unselectedContentColor = ProtonTheme.colors.iconWeak,
                )
            }
        }
        Divider(color = ProtonTheme.colors.separatorNorm)
    }
}

@Preview
@Composable
fun PreviewProtonBottomNavigationTab() {
    val tabs = listOf(
        NavigationTab(R.string.presentation_menu_item_title_settings, R.drawable.ic_logo_mail),
        NavigationTab(R.string.presentation_menu_item_title_settings, R.drawable.ic_logo_drive),
        NavigationTab(R.string.presentation_menu_item_title_settings, R.drawable.ic_logo_vpn)
    )
    ProtonBottomNavigation(
        tabs = tabs,
        onSelectedTabIndex = {}
    )
}

@Preview
@Composable
fun PreviewProtonBottomNavigationImageVector() {
    val tabs = listOf(
        NavigationTab(R.string.presentation_menu_item_title_settings, Icons.Filled.Email),
        NavigationTab(R.string.presentation_menu_item_title_settings, Icons.Filled.ShoppingCart),
        NavigationTab(R.string.presentation_menu_item_title_settings, Icons.Filled.Home),
    )
    ProtonBottomNavigation(
        tabs = tabs,
        onSelectedTabIndex = {},
        initialSelectedTabIndex = 1
    )
}

@Preview
@Composable
fun PreviewProtonBottomNavigationDark() {
    ProtonTheme(colors = ProtonColors.Dark) {
        PreviewProtonBottomNavigationImageVector()
    }
}
