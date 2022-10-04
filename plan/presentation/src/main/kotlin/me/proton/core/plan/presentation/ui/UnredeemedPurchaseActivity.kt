/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.plan.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.ActivityUnredeemedPurchaseBinding
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchase
import me.proton.core.plan.presentation.viewmodel.UnredeemedPurchaseViewModel
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.utils.errorToast
import me.proton.core.presentation.utils.showToast
import me.proton.core.util.kotlin.exhaustive

/** Activity for checking and redeeming Google purchases.
 * The check will be performed against the primary (logged in) user.
 */
@AndroidEntryPoint
class UnredeemedPurchaseActivity :
    ProtonViewBindingActivity<ActivityUnredeemedPurchaseBinding>(ActivityUnredeemedPurchaseBinding::inflate) {

    private val viewModel by viewModels<UnredeemedPurchaseViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreenLayout()
        viewModel.state.onEach(this::handleState).launchIn(lifecycleScope)
    }

    private fun handleState(state: UnredeemedPurchaseViewModel.State) {
        when (state) {
            UnredeemedPurchaseViewModel.State.Loading -> {
                binding.progress.isVisible = true
            }
            is UnredeemedPurchaseViewModel.State.UnredeemedPurchase -> {
                binding.progress.isVisible = false
                showAlertForUnredeemedGooglePurchase(state.unredeemedPurchase, state.userId)
            }
            is UnredeemedPurchaseViewModel.State.Error -> {
                errorToast(getString(R.string.payments_giap_redeem_error))
                cancelAndFinish()
            }
            UnredeemedPurchaseViewModel.State.NoUnredeemedPurchases -> {
                successAndFinish(null)
            }
            UnredeemedPurchaseViewModel.State.PurchaseRedeemed -> {
                showToast(R.string.payments_giap_redeem_success)
                successAndFinish(Result.PurchaseRedeemed)
            }
        }.exhaustive
    }

    private fun makeFullScreenLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    private fun showAlertForUnredeemedGooglePurchase(
        unredeemedPurchase: UnredeemedGooglePurchase,
        userId: UserId
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.payments_giap_unredeemed_title)
            .setMessage(R.string.payments_giap_unredeemed_description)
            .setPositiveButton(R.string.payments_giap_unredeemed_confirm) { _, _ ->
                viewModel.redeemPurchase(unredeemedPurchase, userId)
            }
            .setNegativeButton(R.string.presentation_alert_cancel) { _, _ -> }
            .setOnCancelListener { cancelAndFinish() }
            .show()
    }

    private fun cancelAndFinish() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun successAndFinish(result: Result?) {
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(RESULT_ARG, result?.ordinal)
            }
        )
        finish()
    }

    enum class Result {
        PurchaseRedeemed
    }

    class Start : ActivityResultContract<Unit, Result?>() {
        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(context, UnredeemedPurchaseActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?): Result? {
            if (resultCode != RESULT_OK) return null
            val ordinal = intent?.getIntExtra(RESULT_ARG, -1)
            return if (ordinal != null && ordinal >= 0) {
                Result.values()[ordinal]
            } else {
                null
            }
        }
    }

    companion object {
        private const val RESULT_ARG = "RESULT_ARG"
    }
}
