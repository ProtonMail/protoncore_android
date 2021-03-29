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

package me.proton.core.humanverification.presentation.viewmodel.verification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.proton.core.country.presentation.entity.CountryUIModel
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.usecase.VerifyCode
import me.proton.core.humanverification.presentation.exception.TokenCodeVerificationException
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import studio.forface.viewstatestore.LockedViewStateStore
import studio.forface.viewstatestore.ViewState
import studio.forface.viewstatestore.ViewStateStore

/**
 * View model class that handles and supports [TokenType.CAPTCHA] verification method (type) fragment.
 *
 * @author Dino Kadrikj.
 */
internal class HumanVerificationCaptchaViewModel @ViewModelInject
constructor(
    private val verifyCode: VerifyCode,
    private val networkManager: NetworkManager
) : ProtonViewModel(), HumanVerificationCode {

    /**
     * Code is sometimes referred as a token, so token on BE and code on UI, it is same thing.
     */
    val codeVerificationResult = ViewStateStore<Boolean>().lock
    val networkConnectionState = ViewStateStore<Boolean>().lock

    private val validationCaptcha = ViewStateStore<List<CountryUIModel>>(ViewState.Loading).lock
    private val verificationCodeStatusCaptcha = ViewStateStore<Boolean>().lock

    override val validation: LockedViewStateStore<List<CountryUIModel>>
        get() = validationCaptcha

    override val verificationCodeStatus: LockedViewStateStore<Boolean>
        get() = verificationCodeStatusCaptcha

    init {
        networkWatcher()
    }

    /**
     * Contacts the API and sends the human verification token code.
     */
    fun verifyTokenCode(sessionId: SessionId, token: String?) {
        requireNotNull(token)
        if (token.isEmpty()) {
            throw IllegalArgumentException("Verification token is empty.")
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = verifyCode(sessionId, TokenType.CAPTCHA.name, token)
            if (result is VerificationResult.Success) {
                codeVerificationResult.post(true)
            } else {
                codeVerificationResult.postError(TokenCodeVerificationException())
            }
        }
    }

    /**
     * Watches for any network changes and informs the UI for any state change so that it can act
     * accordingly for any network dependent tasks.
     */
    private fun networkWatcher() {
        viewModelScope.launch {
            networkManager.observe().collect { status ->
                networkConnectionState.postData(
                    data =
                    when (status) {
                        NetworkStatus.Metered, NetworkStatus.Unmetered -> true
                        else -> false
                    }, dropOnSame = true
                )
            }
        }
    }
}
