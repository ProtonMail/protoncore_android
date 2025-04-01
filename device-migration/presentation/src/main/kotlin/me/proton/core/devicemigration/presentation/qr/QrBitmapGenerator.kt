/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.presentation.qr

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import me.proton.core.util.kotlin.DispatcherProvider
import javax.inject.Inject

internal class QrBitmapGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineDispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(
        contents: String,
        size: Dp,
        encoding: QrScanEncoding<*> = QrScanEncoding.default,
        margin: Dp = 0.dp
    ): Bitmap = withContext(coroutineDispatcherProvider.Comp) {
        val encoder = BarcodeEncoder()
        val density = Density(context)
        val sizePx = with(density) { size.roundToPx() }
        val marginPx = with(density) { margin.roundToPx() }

        encoder.encodeBitmap(
            contents,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx,
            mapOf(
                EncodeHintType.CHARACTER_SET to encoding.charset.name(),
                EncodeHintType.MARGIN to marginPx
            )
        )
    }
}
