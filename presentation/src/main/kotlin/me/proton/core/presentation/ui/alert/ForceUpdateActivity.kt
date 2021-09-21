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

package me.proton.core.presentation.ui.alert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Informs the user that an app update is required to proceed.
 * The activity cannot be dismissed (back navigation is disabled).
 * Wraps [ForceUpdateDialog] inside a transparent activity.
 */
class ForceUpdateActivity : AppCompatActivity() {
    companion object {
        private const val ARG_API_ERROR_MESSAGE = "arg.apiErrorMessage"
        private const val ARG_LEARN_MORE_URL = "arg.learnMoreUrl"
        private const val TAG_FORCE_UPDATE_FRAGMENT = "tag.forceUpdateDialog"

        operator fun invoke(
            context: Context,
            apiErrorMessage: String,
            learnMoreURL: String? = null
        ): Intent {
            return Intent(context, ForceUpdateActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra(ARG_API_ERROR_MESSAGE, apiErrorMessage)
                putExtra(ARG_LEARN_MORE_URL, learnMoreURL)
            }
        }
    }

    private val apiErrorMessage: String
        get() = requireNotNull(intent.getStringExtra(ARG_API_ERROR_MESSAGE)) { "Missing `apiErrorMessage` argument" }
    private val learnMoreURL: String? get() = intent.getStringExtra(ARG_LEARN_MORE_URL)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportFragmentManager.findFragmentByTag(TAG_FORCE_UPDATE_FRAGMENT) == null) {
            val dialogFragment = ForceUpdateDialog(apiErrorMessage, learnMoreURL, finishActivityOnBackPress = false)
            dialogFragment.show(supportFragmentManager, TAG_FORCE_UPDATE_FRAGMENT)
        }
    }

    override fun onBackPressed() {
        // back navigation is disabled
    }
}
