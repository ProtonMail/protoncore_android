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
import android.os.Bundle
import android.provider.Browser
import androidx.activity.result.contract.ActivityResultContract
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_DARK
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_OFF
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.net.toUri
import me.proton.core.auth.presentation.entity.AddAccountInput
import me.proton.core.auth.presentation.entity.AddAccountResult
import me.proton.core.auth.presentation.entity.ChooseAddressInput
import me.proton.core.auth.presentation.entity.ChooseAddressResult
import me.proton.core.auth.presentation.entity.DeviceSecretResult
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.LoginSsoInput
import me.proton.core.auth.presentation.entity.LoginSsoResult
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.SecondFactorResult
import me.proton.core.auth.presentation.entity.TwoPassModeInput
import me.proton.core.auth.presentation.entity.TwoPassModeResult
import me.proton.core.auth.presentation.entity.signup.SignUpInput
import me.proton.core.auth.presentation.entity.signup.SignUpResult
import me.proton.core.auth.presentation.ui.signup.SignupActivity

object StartAddAccount : ActivityResultContract<AddAccountInput, AddAccountResult?>() {

    override fun createIntent(context: Context, input: AddAccountInput) =
        Intent(context, AddAccountActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(AddAccountActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): AddAccountResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(AddAccountActivity.ARG_RESULT)
    }
}

object StartLogin : ActivityResultContract<LoginInput, LoginResult?>() {

    override fun createIntent(context: Context, input: LoginInput) =
        Intent(context, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(LoginActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): LoginResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(LoginActivity.ARG_RESULT)
    }
}

object StartLoginTwoStep : ActivityResultContract<LoginInput, LoginResult?>() {

    override fun createIntent(context: Context, input: LoginInput) =
        Intent(context, LoginTwoStepActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(LoginActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): LoginResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(LoginActivity.ARG_RESULT)
    }
}

object StartLoginSso : ActivityResultContract<LoginSsoInput, LoginSsoResult?>() {

    override fun createIntent(context: Context, input: LoginSsoInput) =
        Intent(context, LoginSsoActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(LoginSsoActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): LoginSsoResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(LoginSsoActivity.ARG_RESULT)
    }
}

object StartSecondFactor : ActivityResultContract<SecondFactorInput, SecondFactorResult?>() {

    override fun createIntent(context: Context, input: SecondFactorInput) =
        Intent(context, SecondFactorActivity::class.java).apply {
            putExtra(SecondFactorActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): SecondFactorResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(SecondFactorActivity.ARG_RESULT)
    }
}

object StartTwoPassMode : ActivityResultContract<TwoPassModeInput, TwoPassModeResult?>() {

    override fun createIntent(context: Context, input: TwoPassModeInput) =
        Intent(context, TwoPassModeActivity::class.java).apply {
            putExtra(TwoPassModeActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): TwoPassModeResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(TwoPassModeActivity.ARG_RESULT)
    }
}

object StartChooseAddress : ActivityResultContract<ChooseAddressInput, ChooseAddressResult?>() {

    override fun createIntent(context: Context, input: ChooseAddressInput) =
        Intent(context, ChooseAddressActivity::class.java).apply {
            putExtra(ChooseAddressActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): ChooseAddressResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(ChooseAddressActivity.ARG_RESULT)
    }
}

object StartDeviceSecret : ActivityResultContract<String, DeviceSecretResult?>() {

    override fun createIntent(context: Context, input: String) =
        Intent(context, DeviceSecretActivity::class.java).apply {
            putExtra(DeviceSecretActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): DeviceSecretResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(DeviceSecretActivity.ARG_RESULT)
    }
}

object StartSignup : ActivityResultContract<SignUpInput, SignUpResult?>() {

    override fun createIntent(context: Context, input: SignUpInput) =
        Intent(context, SignupActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(SignupActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): SignUpResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(SignupActivity.ARG_RESULT)
    }
}

object StartCustomTab : ActivityResultContract<StartCustomTab.Input, Boolean>() {

    data class Input(val url: String, val headers: Bundle, val session: CustomTabsSession?)

    override fun createIntent(context: Context, input: Input): Intent =
        CustomTabsIntent.Builder().apply {
            input.session?.let { setSession(it) }
            setShowTitle(false)
            setBookmarksButtonEnabled(false)
            setDownloadButtonEnabled(false)
            setUrlBarHidingEnabled(false)
            setColorScheme(COLOR_SCHEME_DARK)
            setShareState(SHARE_STATE_OFF)
        }.build().apply {
            intent.putExtra(Browser.EXTRA_HEADERS, input.headers)
            intent.data = input.url.toUri()
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }.intent

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}
