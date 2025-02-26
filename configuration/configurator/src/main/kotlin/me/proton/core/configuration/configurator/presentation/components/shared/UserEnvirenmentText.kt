package me.proton.core.configuration.configurator.presentation.components.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import me.proton.core.configuration.configurator.presentation.viewModel.CreateUserViewModel
import me.proton.core.configuration.configurator.presentation.viewModel.SharedData

@Composable
fun UserEnvironmentText(selectedDomain: String, sharedData: SharedData) {
    var envUserText: String = ""

    envUserText = if (sharedData.lastUsername.isNotEmpty()) {
        "$selectedDomain | ${sharedData.lastUsername}"
    } else {
        selectedDomain
    }

    Text(
        modifier = Modifier.fillMaxWidth(),
        text = envUserText,
//        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}