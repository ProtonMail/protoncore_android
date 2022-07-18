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
package me.proton.core.compose.activity

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Deprecated(
    message = "This composable is deprecated",
    replaceWith = ReplaceWith(
        """
            rememberOpenMultipleDocumentsLauncher(
                mimeTypes: Array<String> = arrayOf("*/*"),
                modifyIntent: ((Intent) -> Unit)? = null,
                onFilesPicked: (List<Uri>) -> Unit
            )
        """
    )
)
@Composable
fun rememberFilePickerLauncher(
    onFilesPicked: (List<Uri>) -> Unit,
    input: Array<String> = arrayOf("*/*"),
): FilePickerLauncher {
    var result by remember { mutableStateOf<List<Uri>?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { fileUri ->
        result = fileUri.orEmpty()
    }
    LaunchedEffect(result) {
        result?.run {
            onFilesPicked(this)
            result = null
        }
    }
    return FilePickerLauncher(launcher, input)
}

class FilePickerLauncher(
    private val launcher: ManagedActivityResultLauncher<Array<String>, List<Uri>>,
    private val input: Array<String>,
) {
    fun showPicker() = launcher.launch(input)
}
