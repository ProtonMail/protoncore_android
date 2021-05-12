/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.auth.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import me.proton.core.auth.presentation.entity.ChooseAddressInput
import me.proton.core.auth.presentation.entity.ChooseAddressResult
import me.proton.core.auth.presentation.entity.CreateAddressInput
import me.proton.core.auth.presentation.entity.CreateAddressResult
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.SecondFactorResult
import me.proton.core.auth.presentation.entity.TwoPassModeInput
import me.proton.core.auth.presentation.entity.TwoPassModeResult
import me.proton.core.auth.presentation.entity.signup.SignUpInput
import me.proton.core.auth.presentation.entity.signup.SignUpResult
import me.proton.core.auth.presentation.ui.signup.SignupActivity

class StartLogin : ActivityResultContract<LoginInput, LoginResult?>() {

    override fun createIntent(context: Context, input: LoginInput) =
        Intent(context, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            putExtra(LoginActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, result: Intent?): LoginResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(LoginActivity.ARG_RESULT)
    }
}

class StartSecondFactor : ActivityResultContract<SecondFactorInput, SecondFactorResult?>() {

    override fun createIntent(context: Context, inupt: SecondFactorInput) =
        Intent(context, SecondFactorActivity::class.java).apply {
            putExtra(SecondFactorActivity.ARG_INPUT, inupt)
        }

    override fun parseResult(resultCode: Int, result: Intent?): SecondFactorResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(SecondFactorActivity.ARG_RESULT)
    }
}

class StartTwoPassMode : ActivityResultContract<TwoPassModeInput, TwoPassModeResult?>() {

    override fun createIntent(context: Context, inupt: TwoPassModeInput) =
        Intent(context, TwoPassModeActivity::class.java).apply {
            putExtra(TwoPassModeActivity.ARG_INPUT, inupt)
        }

    override fun parseResult(resultCode: Int, result: Intent?): TwoPassModeResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(TwoPassModeActivity.ARG_RESULT)
    }
}

class StartChooseAddress : ActivityResultContract<ChooseAddressInput, ChooseAddressResult?>() {

    override fun createIntent(context: Context, input: ChooseAddressInput) =
        Intent(context, ChooseAddressActivity::class.java).apply {
            putExtra(ChooseAddressActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, result: Intent?): ChooseAddressResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(ChooseAddressActivity.ARG_RESULT)
    }
}

class StartCreateAddress : ActivityResultContract<CreateAddressInput, CreateAddressResult?>() {

    override fun createIntent(context: Context, input: CreateAddressInput) =
        Intent(context, CreateAddressActivity::class.java).apply {
            putExtra(CreateAddressActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, result: Intent?): CreateAddressResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(CreateAddressActivity.ARG_RESULT)
    }
}

// region signup
class StartSignup : ActivityResultContract<SignUpInput, SignUpResult?>() {

    override fun createIntent(context: Context, input: SignUpInput?) =
        Intent(context, SignupActivity::class.java).apply {
            putExtra(SignupActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, result: Intent?): SignUpResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(SignupActivity.ARG_RESULT)
    }
}

// endregion
