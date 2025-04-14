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

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat.getInsetsController
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.Size
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.devicemigration.presentation.R
import me.proton.core.devicemigration.presentation.databinding.ActivityEdmQrCaptureBinding
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.EdmScreenViewTotal
import me.proton.core.presentation.utils.doOnApplyWindowInsets
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.viewBinding
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Modeled after [com.journeyapps.barcodescanner.CaptureActivity].
 */
@AndroidEntryPoint
public class EdmQrCaptureActivity : ComponentActivity() {
    @Inject
    internal lateinit var observabilityManager: ObservabilityManager

    private val binding: ActivityEdmQrCaptureBinding by viewBinding(ActivityEdmQrCaptureBinding::inflate)
    private lateinit var capture: CaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initEventListeners()
        adjustLayout()

        capture = CaptureManager(this, binding.zxingBarcodeScanner).apply {
            initializeFromIntent(intent, savedInstanceState)
            decode()
        }
        capture.setShowMissingCameraPermissionDialog(false)

        launchOnScreenView {
            observabilityManager.enqueue(EdmScreenViewTotal(EdmScreenViewTotal.ScreenId.origin_qr_code_input))
        }
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }


    @Deprecated("Deprecated in androidx-activity, but required for CaptureManager.")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return binding.zxingBarcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    private fun initEventListeners() {
        binding.closeButton.onClick {
            setResult(RESULT_CANCELED)
            finish()
        }
        binding.enterCodeButton.onClick {
            setResult(RESULT_CANCELED, Intent().putExtra(RESULT_MANUAL_INPUT_REQUESTED, true))
            finish()
        }
    }

    private fun adjustLayout() {
        binding.zxingBarcodeScanner.doOnLayout { view ->
            val a = (min(view.width, view.height) * SIZE_MULTIPLIER).roundToInt()
            binding.zxingBarcodeScanner.barcodeView.framingRectSize = Size(a, a)
            binding.zxingBarcodeScanner.statusView.updateLayoutParams<MarginLayoutParams> {
                topMargin = (view.height / 2) + (a / 2) + resources.getDimensionPixelSize(R.dimen.gap_large)
            }
        }

        binding.enterCodeButton.doOnApplyWindowInsets { view, windowInsetsCompat, initialMargin, _ ->
            val bars = windowInsetsCompat.getInsets(systemBars())
            view.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = bars.bottom + initialMargin.bottom
            }
        }

        binding.closeButton.doOnApplyWindowInsets { view, windowInsetsCompat, initialMargin, _ ->
            val bars = windowInsetsCompat.getInsets(displayCutout() or systemBars())
            view.updateLayoutParams<MarginLayoutParams> {
                topMargin = bars.top + initialMargin.top
            }
        }
    }

    internal companion object {
        const val RESULT_MANUAL_INPUT_REQUESTED = "RESULT_MANUAL_INPUT_REQUESTED"
        private const val SIZE_MULTIPLIER = 0.8
    }
}
