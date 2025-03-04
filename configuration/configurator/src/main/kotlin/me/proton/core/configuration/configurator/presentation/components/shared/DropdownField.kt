package me.proton.core.configuration.configurator.presentation.components.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import me.proton.core.compose.theme.ProtonDimens

@Composable
fun DropdownField(
    modifier: Modifier = Modifier,
    label: String = "",
    options: List<String>,
    selectedOption: String,
    enabled: Boolean = true,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .padding(top = ProtonDimens.SmallSpacing)
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Column {
            if (label.isNotEmpty()) {
                Text(text = label, style = MaterialTheme.typography.caption)
            }
            Text(text = selectedOption,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded && enabled }
                    .padding(vertical = ProtonDimens.DefaultSpacing)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onOptionSelected(option)
                    expanded = false
                }) {
                    Text(option)
                }
            }
        }
    }
}
