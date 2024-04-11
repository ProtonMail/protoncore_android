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

package me.proton.core.configuration.configurator.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonDimens

@Composable
fun SearchableConfigurationTextField(
    searchData: List<String>,
    onResultSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    // State for the search text
    var searchText by remember { mutableStateOf("") }

    // Filter the search data based on the search text
    val filteredItems = if (searchText.isEmpty()) searchData else searchData.filter {
        it.contains(searchText, ignoreCase = true)
    }

    Surface {
        Column(modifier = Modifier.height(300.dp)) {
            // Top App Bar with a TextField for searching
            TopAppBar(
                title = {
                    BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (searchText.isEmpty()) {
                                Text("Search", color = Color.LightGray)
                            }
                            innerTextField()
                        }
                    )
                },
                actions = {
                    // Wrap 'Cancel' text in clickable modifier to handle taps
                    Text(
                        text = "Cancel",
                        modifier = Modifier
                            .padding(horizontal = ProtonDimens.SmallSpacing)
                            .clickable { onDismissRequest() }, // This calls the provided `onDismissRequest` callback
                        color = Color.White
                    )
                }
            )

            // List of filtered items
            LazyColumn {
                items(filteredItems, key = { it }) { item ->
                    ItemView(item = item, onItemClicked = onResultSelected)
                }
            }
        }
    }
}

@Composable
fun ItemView(item: String, onItemClicked: (String) -> Unit) {
    Text(
        text = item,
        modifier = Modifier
            .fillMaxWidth()
            .padding(ProtonDimens.SmallSpacing)
            .clickable { onItemClicked(item) }
    )
}
