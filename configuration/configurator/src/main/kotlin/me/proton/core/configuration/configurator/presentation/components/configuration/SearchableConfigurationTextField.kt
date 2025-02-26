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

package me.proton.core.configuration.configurator.presentation.components.configuration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.window.PopupProperties
import me.proton.core.compose.component.ProtonCloseButton
import me.proton.core.compose.component.ProtonOutlinedTextField
import me.proton.core.compose.theme.ProtonDimens

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchableConfigurationTextField(
    searchData: List<String>,
    onResultSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    // State for the search text
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        expanded = true
    }

    // Filter the search data based on the search text
    val filteredItems = if (searchText.isEmpty()) searchData else searchData.filter {
        it.contains(searchText, ignoreCase = true)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { isExpanded ->
            expanded = isExpanded
            if (isExpanded) {
                focusRequester.requestFocus()
            }
        }
    ) {
        ProtonOutlinedTextField(
            label = { Text(text = "search") },
            modifier = Modifier
                .bottomPad(ProtonDimens.SmallSpacing)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) expanded = filteredItems.isNotEmpty()
                },
            value = TextFieldValue(AnnotatedString(searchText), TextRange(searchText.length)),
            onValueChange = {
                searchText = it.text
                expanded = filteredItems.isNotEmpty()
            },
            singleLine = true,
            trailingIcon = {
                ProtonCloseButton(
                    onCloseClicked = {
                        onDismissRequest()
                        expanded = false
                    },
                )
            })

        // Dropdown Menu anchored below the search field
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier
                .exposedDropdownSize(true)
        ) {
            filteredItems.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onResultSelected(item)
                        searchText = item
                        expanded = false // Close dropdown
                    }
                ) {
                    Text(text = item)
                }
            }
        }
    }
}



@Composable
fun ItemView(item: String, onItemClicked: (String) -> Unit) {
    Text(text = item,
        modifier = Modifier
            .fillMaxWidth()
            .padding(ProtonDimens.SmallSpacing)
            .clickable { onItemClicked(item) })
}
