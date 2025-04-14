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
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import me.proton.core.devicemigration.presentation.qr.EdmQrCaptureActivity.Companion.RESULT_MANUAL_INPUT_REQUESTED

internal class QrScanContract<T : Any>(
    private val encoding: QrScanEncoding<T>
) : ActivityResultContract<Unit, QrScanOutput<T>>() {
    private val scanContract = ScanContract()

    override fun createIntent(context: Context, input: Unit): Intent {
        val opts = ScanOptions().apply {
            addExtra(Intents.Scan.CHARACTER_SET, encoding.charset.name())
            setBeepEnabled(false)
            setCaptureActivity(EdmQrCaptureActivity::class.java)
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setOrientationLocked(false)
        }
        return scanContract.createIntent(context, opts)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): QrScanOutput<T> {
        val result = scanContract.parseResult(resultCode, intent)
        return when {
            result.hasContents() -> QrScanOutput.Success(encoding.decode(result.contents))
            result.isManualInputRequested() -> QrScanOutput.ManualInputRequested()
            intent.isMissingCameraPermission() -> QrScanOutput.MissingCameraPermission()
            else -> QrScanOutput.Cancelled()
        }
    }
}

private fun ScanIntentResult.hasContents(): Boolean = contents?.isNotEmpty() == true

private fun ScanIntentResult.isManualInputRequested(): Boolean =
    originalIntent?.getBooleanExtra(RESULT_MANUAL_INPUT_REQUESTED, false) == true

private fun Intent?.isMissingCameraPermission(): Boolean =
    this?.getBooleanExtra(Intents.Scan.MISSING_CAMERA_PERMISSION, false) == true
