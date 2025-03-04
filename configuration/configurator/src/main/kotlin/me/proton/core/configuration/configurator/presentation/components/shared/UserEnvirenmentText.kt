package me.proton.core.configuration.configurator.presentation.components.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import me.proton.core.configuration.configurator.presentation.viewModel.SharedData

@Composable
fun UserEnvironmentText(selectedDomain: String, sharedData: SharedData) {
    val envUserText = if (sharedData.lastUsername.isNotEmpty()) {
        "$selectedDomain | ${sharedData.lastUsername}"
    } else {
        selectedDomain
    }

    Text(
        modifier = Modifier.fillMaxWidth(),
        text = envUserText,
        textAlign = TextAlign.Center
    )
}
