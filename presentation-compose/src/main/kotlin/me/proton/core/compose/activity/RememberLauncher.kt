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

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable

/**
 * [ManagedActivityResultLauncher] with predefined [input].
 */
class LauncherWithInput<Input, Output>(
    private val input: Input,
    private val launcher: ManagedActivityResultLauncher<Input, Output>,
) {
    fun launch() = launcher.launch(input)
    fun launch(input: Input) = launcher.launch(input)
}

/**
 * Register a [LauncherWithInput].
 *
 * @see [rememberLauncher]
 */
@Composable
fun <Input, Output> rememberLauncherWithInput(
    input: Input,
    contracts: ActivityResultContract<Input, Output>,
    onResult: (Output) -> Unit
): LauncherWithInput<Input, Output> = LauncherWithInput(
    input = input,
    launcher = rememberLauncher(contracts, onResult)
)

/**
 * Register a [ManagedActivityResultLauncher].
 *
 * @see [rememberLauncherForActivityResult]
 */
@Composable
fun <Input, Output> rememberLauncher(
    contracts: ActivityResultContract<Input, Output>,
    onResult: (Output) -> Unit
): ManagedActivityResultLauncher<Input, Output> = rememberLauncherForActivityResult(contracts, onResult)
