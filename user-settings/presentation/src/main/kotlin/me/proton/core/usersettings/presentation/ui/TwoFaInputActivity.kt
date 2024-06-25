/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.usersettings.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.viewmodel.Source
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.usersettings.presentation.entity.TwoFaDialogArguments

@AndroidEntryPoint
class TwoFaInputActivity : ProtonActivity() {

    private val twoFaDialogArguments by lazy {
        intent.getParcelableExtra<TwoFaDialogArguments>(ARG_INPUT) ?: error("Missing activity input")
    }

    private val source by lazy {
        twoFaDialogArguments.source
    }

    private val userId by lazy {
        twoFaDialogArguments.user
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val twoFactorLauncher =
            supportFragmentManager.registerShowTwoFADialogResultLauncher(this, source, userId) { result ->
                val intent = Intent().putExtra(ARG_RESULT, result)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

        twoFactorLauncher.show(Unit)
    }

    companion object {
        const val ARG_INPUT = "arg.twoFaInput"
        const val ARG_RESULT = "arg.twoFAInputResult"
    }
}