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
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.SecondFactorResult
import me.proton.core.auth.presentation.entity.TwoPassModeInput
import me.proton.core.auth.presentation.entity.TwoPassModeResult
import me.proton.core.auth.presentation.entity.UserResult
import me.proton.core.network.domain.session.SessionId

class StartLogin : ActivityResultContract<LoginInput, LoginResult?>() {

    override fun createIntent(context: Context, input: LoginInput) =
        Intent(context, LoginActivity::class.java).apply {
            putExtra(LoginActivity.ARG_LOGIN_INPUT, input)
        }

    override fun parseResult(resultCode: Int, result: Intent?): LoginResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(LoginActivity.ARG_LOGIN_RESULT)
    }
}

class StartSecondFactor : ActivityResultContract<SecondFactorInput, SecondFactorResult?>() {

    override fun createIntent(context: Context, inupt: SecondFactorInput) =
        Intent(context, SecondFactorActivity::class.java).apply {
            putExtra(SecondFactorActivity.ARG_SECOND_FACTOR_INPUT, inupt)
        }

    override fun parseResult(resultCode: Int, result: Intent?): SecondFactorResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(SecondFactorActivity.ARG_SECOND_FACTOR_RESULT)
    }
}

class StartTwoPassMode : ActivityResultContract<TwoPassModeInput, TwoPassModeResult?>() {

    override fun createIntent(context: Context, inupt: TwoPassModeInput) =
        Intent(context, MailboxLoginActivity::class.java).apply {
            putExtra(MailboxLoginActivity.ARG_SESSION_ID, inupt.sessionId)
            putExtra(MailboxLoginActivity.ARG_REQUIRED_ACCOUNT_TYPE, inupt.requiredAccountType.name)
        }

    override fun parseResult(resultCode: Int, result: Intent?): TwoPassModeResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(MailboxLoginActivity.ARG_MAILBOX_LOGIN_RESULT)
    }
}

class StartUsernameChooseForAccountUpgrade : ActivityResultContract<CreateAddressInput, UserResult?>() {

    override fun createIntent(context: Context, input: CreateAddressInput) =
        Intent(context, CreateAddressActivity::class.java).apply {
            putExtra(CreateAddressActivity.ARG_SESSION_ID, input.sessionId.id)
            putExtra(CreateAddressActivity.ARG_USER, input.user)
            putExtra(CreateAddressActivity.ARG_EXTERNAL_EMAIL, input.externalEmail)
        }

    override fun parseResult(resultCode: Int, result: Intent?): UserResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(CreateAddressActivity.ARG_USER_RESULT)
    }
}

class StartAccountUpgrade : ActivityResultContract<UpgradeInput, UserResult?>() {

    override fun createIntent(context: Context, input: UpgradeInput) =
        Intent(context, CreateAddressResultActivity::class.java).apply {
            putExtra(CreateAddressResultActivity.ARG_SESSION_ID, input.sessionId.id)
            putExtra(CreateAddressResultActivity.ARG_USER, input.user)
            putExtra(CreateAddressResultActivity.ARG_USERNAME, input.username)
            putExtra(CreateAddressResultActivity.ARG_DOMAIN, input.domain)
        }

    override fun parseResult(resultCode: Int, result: Intent?): UserResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(CreateAddressResultActivity.ARG_USER_RESULT)
    }
}

data class CreateAddressInput(
    val sessionId: SessionId,
    val externalEmail: String?,
    val user: UserResult
)

data class UpgradeInput(
    val sessionId: SessionId,
    val user: UserResult,
    val username: String,
    val domain: String? = null
)

