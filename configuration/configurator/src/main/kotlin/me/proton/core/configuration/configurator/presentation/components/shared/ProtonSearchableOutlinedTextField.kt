package me.proton.core.configuration.configurator.presentation.components.shared

import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.compose.component.ProtonCloseButton
import me.proton.core.compose.component.ProtonOutlinedTextField
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.configuration.configurator.presentation.components.configuration.ConfigActionButton
import me.proton.core.configuration.configurator.presentation.components.configuration.bottomPad
import me.proton.core.presentation.R

enum class Type {
    SEARCH,
    CANCEL,
    LOADING
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProtonSearchableOutlinedTextField(
    name: String = "",
    expandedState: Boolean,
    value: String = "",
    searchData: List<String>,
    onCancelIconClick: () -> Unit = {},
    onSearchIconClick: () -> Unit = {},
    onResultSelected: (String) -> Unit,
    onValueChange: (TextFieldValue) -> Unit = {},
    iconType: Type = Type.SEARCH,
    loading: Boolean = false
) {
    // State for the search text
    var searchText by remember { mutableStateOf(value) }
    var expanded by remember { mutableStateOf(expandedState) }
    var iconButtonType by remember { mutableStateOf(iconType) }
    val focusRequester = remember { FocusRequester() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange =
        { isExpanded ->
            expanded = isExpanded
            if (isExpanded) {
                focusRequester.requestFocus()
            }
        }
    )
    {
        ProtonOutlinedTextField(
            label = { Text(text = name) },
            modifier = Modifier
                .bottomPad(ProtonDimens.SmallSpacing)
                .focusRequester(focusRequester),
            value = TextFieldValue(AnnotatedString(searchText), TextRange(searchText.length)),
            onValueChange = {
                searchText = it.text
                onValueChange(it)
            },
            singleLine = true,
            trailingIcon = {

                iconButtonType = if (loading) {
                    Type.LOADING
                } else if (searchText.isNotEmpty()) {
                    Type.CANCEL
                } else {
                    Type.SEARCH
                }

                when (iconButtonType) {
                    Type.SEARCH -> {
                        ConfigActionButton(
                            drawableId = R.drawable.abc_ic_search_api_material,
                            onClick = {
                                onSearchIconClick()
                            }
                        )
                    }
                    Type.LOADING -> {
                        IconButton(
                            onClick = {}
                        ) {
                            ProtonCenteredProgress(modifier = Modifier.size(24.dp))
                        }
                        expanded = false
                    }
                    Type.CANCEL ->
                        ProtonCloseButton(
                            onCloseClicked = {
                                onCancelIconClick()
                                expanded = false
                                searchText = ""
                            },
                        )
                }
            })
        // Filter the search data based on the search text
        val filteredItems by remember(searchText, searchData) {
            derivedStateOf {
                if (searchText.isEmpty()) searchData
                else searchData.filter { it.contains(searchText, ignoreCase = true) }
            }
        }
        if (filteredItems.isNotEmpty()) {

            // Dropdown Menu anchored below the search field
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .exposedDropdownSize(true)
            ) {
                filteredItems.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            onResultSelected(item)
                            searchText = item
                            expanded = false
                        }
                    ) {
                        Text(text = item)
                    }
                }
            }
        }
    }
}