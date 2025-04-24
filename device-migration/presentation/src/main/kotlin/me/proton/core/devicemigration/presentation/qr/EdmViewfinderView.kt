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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.times
import androidx.core.graphics.toRectF
import com.journeyapps.barcodescanner.ViewfinderView
import me.proton.core.devicemigration.presentation.R

private const val FRAME_LINES_RECT_MULTIPLIER = 1.05f

public class EdmViewfinderView(context: Context, attrs: AttributeSet) : ViewfinderView(context, attrs) {
    private val frameLines = ResourcesCompat.getDrawable(resources, R.drawable.edm_qr_square, null)?.apply {
        DrawableCompat.setTint(this, Color.WHITE)
    }
    private val radius = with(Density(context)) { 14.dp.toPx() }
    private val transparentPaint = Paint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onDraw(canvas: Canvas) {
        refreshSizes()
        if (framingRect == null || previewSize == null) {
            return
        }

        val frame = framingRect
        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()

        val saveCount = canvas.saveLayer(0f, 0f, canvasWidth, canvasHeight, null)
        paint.color = if (resultBitmap != null) resultColor else maskColor
        canvas.drawRect(0f, 0f, canvasWidth, canvasHeight, paint)

        // Draw a "whole":
        canvas.drawRoundRect(frame.toRectF(), radius, radius, transparentPaint)
        canvas.restoreToCount(saveCount)

        val frameLinesRect = (frame.toRectF() * FRAME_LINES_RECT_MULTIPLIER).apply {
            // Center the frame lines in the canvas:
            offsetTo(canvasWidth / 2 - width() / 2, canvasHeight / 2 - height() / 2)
        }
        canvas.drawBitmap(frameLines!!.toBitmap(), null, frameLinesRect, null)
    }
}
