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

import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun ProtonSnackbarHost(
    hostState: ProtonSnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { data ->
        ProtonSnackbar(snackbarData = data, hostState.type)
    },
) {
    SnackbarHost(
        hostState = hostState.snackbarHostState,
        modifier = modifier,
        snackbar = snackbar
    )
}

@Stable
class ProtonSnackbarHostState(
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    defaultType: ProtonSnackbarType = ProtonSnackbarType.WARNING
) {

    private val mutex = Mutex()

    var type by mutableStateOf(defaultType)
        private set

    suspend fun showSnackbar(
        type: ProtonSnackbarType,
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ): SnackbarResult = mutex.withLock {
        this.type = type
        snackbarHostState.showSnackbar(message, actionLabel, duration)
    }
}

@Composable
fun ProtonSnackbar(
    snackbarData: SnackbarData,
    type: ProtonSnackbarType,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = ProtonTheme.shapes.small,
    contentColor: Color = ProtonTheme.colors.textInverted,
    actionColor: Color = ProtonTheme.colors.textInverted,
    elevation: Dp = 6.dp,
) {
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        backgroundColor = when (type) {
            ProtonSnackbarType.SUCCESS -> ProtonTheme.colors.notificationSuccess
            ProtonSnackbarType.WARNING -> ProtonTheme.colors.notificationWarning
            ProtonSnackbarType.ERROR -> ProtonTheme.colors.notificationError
            ProtonSnackbarType.NORM -> ProtonTheme.colors.notificationNorm
        },
        contentColor = contentColor,
        actionColor = actionColor,
        elevation = elevation
    )
}

private val previewSnackbarData = object : SnackbarData {
    override val actionLabel: String? = null
    override val duration: SnackbarDuration = SnackbarDuration.Indefinite
    override val message: String = "This is a snackbar"
    override fun dismiss() = Unit
    override fun performAction() = Unit
}

@Preview
@Composable
@Suppress("unused")
private fun PreviewSuccessSnackbar() {
    ProtonTheme {
        ProtonSnackbar(snackbarData = previewSnackbarData, type = ProtonSnackbarType.SUCCESS)
    }
}

@Preview
@Composable
@Suppress("unused")
private fun PreviewErrorSnackbar() {
    ProtonTheme {
        ProtonSnackbar(snackbarData = previewSnackbarData, type = ProtonSnackbarType.ERROR)
    }
}

@Preview
@Composable
@Suppress("unused")
private fun PreviewWarningSnackbar() {
    ProtonTheme {
        ProtonSnackbar(snackbarData = previewSnackbarData, type = ProtonSnackbarType.WARNING)
    }
}

@Preview
@Composable
@Suppress("unused")
private fun PreviewNormSnackbar() {
    ProtonTheme {
        ProtonSnackbar(snackbarData = previewSnackbarData, type = ProtonSnackbarType.NORM)
    }
}

enum class ProtonSnackbarType {
    SUCCESS, WARNING, ERROR, NORM
}
