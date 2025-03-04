package me.proton.core.configuration.configurator.presentation.components.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import me.proton.core.compose.component.ProtonOutlinedTextField
import me.proton.core.configuration.configurator.presentation.components.configuration.ConfigActionButton
import me.proton.core.presentation.R

enum class Type {
    SEARCH,
    CANCEL,
    LOADING
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProtonSearchableOutlinedTextField(
    modifier: Modifier = Modifier,
    name: String = "",
    value: String = "",
    searchData: List<String>,
    onCancelIconClick: () -> Unit = {},
    onSearchIconClick: () -> Unit = {},
    onResultSelected: (String) -> Unit,
    onValueChange: (TextFieldValue) -> Unit = {},
    iconType: Type = Type.SEARCH,
    loading: Boolean = false,
    enabled: Boolean = true
) {
    // State for the search text
    var searchText by remember { mutableStateOf(value) }
    var expanded by remember { mutableStateOf(false) }
    var iconButtonType by remember { mutableStateOf(iconType) }
    val focusRequester = remember { FocusRequester() }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { isExpanded ->
            expanded = isExpanded && enabled
            if (isExpanded) {
                focusRequester.requestFocus()
            }
        }
    )
    {
        ProtonOutlinedTextField(
            enabled = enabled,
            label = { Text(text = name) },
            modifier = Modifier
                .fillMaxWidth()
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
                            enabled = enabled,
                            drawableId = R.drawable.abc_ic_search_api_material,
                            onClick = {
                                onSearchIconClick()
                            }
                        )
                    }

                    Type.LOADING -> {
                        IconButton(
                            enabled = enabled,
                            onClick = {}
                        ) {
                            ProtonCenteredProgress(modifier = Modifier.size(24.dp))
                        }
                        expanded = false
                    }

                    Type.CANCEL ->
                        ConfigActionButton(
                            enabled = enabled,
                            drawableId = R.drawable.ic_proton_close,
                            onClick = {
                                onCancelIconClick()
                                expanded = false
                                searchText = ""
                            }
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
