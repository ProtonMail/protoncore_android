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
import me.proton.core.auth.presentation.entity.ScopeResult
import me.proton.core.auth.presentation.entity.SessionResult
import me.proton.core.auth.presentation.entity.UserResult
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId

class StartLogin : ActivityResultContract<List<String>, SessionResult?>() {

    override fun createIntent(context: Context, input: List<String>) =
        Intent(context, LoginActivity::class.java).apply {
            putStringArrayListExtra(LoginActivity.ARG_REQUIRED_FEATURES, ArrayList(input))
        }

    override fun parseResult(resultCode: Int, result: Intent?): SessionResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(LoginActivity.ARG_SESSION_RESULT)
    }
}

class StartSecondFactor : ActivityResultContract<SessionId, ScopeResult?>() {

    override fun createIntent(context: Context, sessionId: SessionId) =
        Intent(context, SecondFactorActivity::class.java).apply {
            putExtra(SecondFactorActivity.ARG_SESSION_ID, sessionId.id)
        }

    override fun parseResult(resultCode: Int, result: Intent?): ScopeResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(SecondFactorActivity.ARG_SCOPE_RESULT)
    }
}

class StartMailboxLogin : ActivityResultContract<SessionId, UserResult?>() {

    override fun createIntent(context: Context, sessionId: SessionId) =
        Intent(context, MailboxLoginActivity::class.java).apply {
            putExtra(MailboxLoginActivity.ARG_SESSION_ID, sessionId.id)
        }

    override fun parseResult(resultCode: Int, result: Intent?): UserResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return result?.getParcelableExtra(MailboxLoginActivity.ARG_USER_RESULT)
    }
}
