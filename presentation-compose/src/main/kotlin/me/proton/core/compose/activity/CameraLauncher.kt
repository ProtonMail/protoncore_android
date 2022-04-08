/*
 * Copyright (c) 2022 Proton Technologies AG
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
package me.proton.core.compose.activity

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberCameraLauncher(
    onCaptured: (Boolean) -> Unit
): CameraLauncher {
    var result by remember { mutableStateOf<Boolean?>(null) }
    val onResult = { activityResult: Boolean -> result = activityResult }
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture(), onResult)
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo(), onResult)
    LaunchedEffect(result) {
        result?.run {
            onCaptured(this)
            result = null
        }
    }
    return CameraLauncher(imageLauncher, videoLauncher)
}

enum class Capture {
    IMAGE,
    VIDEO,
}

data class CameraLauncher(
    private val imageLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    private val videoLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
) {
    fun capture(destinationUri: Uri, capture: Capture = Capture.IMAGE) =
        launcher(capture).launch(destinationUri)

    private fun launcher(capture: Capture): ManagedActivityResultLauncher<Uri, Boolean> = when (capture) {
        Capture.IMAGE -> imageLauncher
        Capture.VIDEO -> videoLauncher
    }
}
