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

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun ProtonSidebarLazy(
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    content: LazyListScope.() -> Unit,
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
            val state = rememberLazyListState()

            LazyColumn(
                state = state,
                content = content
            )
        }
    }
}

@Preview(
    name = "Sidebar in light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Sidebar in dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
fun PreviewProtonSidebarLazy() {
    ProtonTheme {
        ProtonSidebarLazy(
            drawerState = DrawerState(DrawerValue.Open) { true },
        ) {
            item {
                ProtonSidebarItem { Text(text = "Inbox") }
                ProtonSidebarItem { Text(text = "Drafts") }
                ProtonSidebarItem { Text(text = "Sent") }
                ProtonSidebarItem(isSelected = true) { Text(text = "Trash (active)") }
                ProtonSidebarItem { Text(text = "All mail") }

                Divider()
                ProtonSidebarItem { Text(text = "Folders", color = ProtonTheme.colors.textHint) }
            }

            val folders = (1..2).map { "Folder $it" }
            items(folders) {
                val icon = rememberVectorPainter(image = Icons.Filled.Favorite)
                ProtonSidebarItem(
                    icon = icon,
                    text = it,
                    textColor = ProtonTheme.colors.textHint,
                    iconTint = Color.Cyan
                )
            }

            item {
                Divider()
                ProtonSidebarItem { Text(text = "Labels", color = ProtonTheme.colors.textHint) }
            }

            val labels = (1..3).map { "Label $it" }
            items(labels) {
                val icon = rememberVectorPainter(image = Icons.Filled.Edit)
                ProtonSidebarItem(
                    icon = icon,
                    text = it,
                    textColor = ProtonTheme.colors.textHint,
                    iconTint = Color.Yellow
                )
            }

            item {
                Divider()
                ProtonSidebarItem { Text(text = "More", color = ProtonTheme.colors.textHint) }
                ProtonSidebarSettingsItem()
                ProtonSidebarReportBugItem()
                ProtonSidebarSignOutItem()

                ProtonSidebarAppVersionItem(name = "App Name", version = "0.0.7")
            }
        }
    }
}
