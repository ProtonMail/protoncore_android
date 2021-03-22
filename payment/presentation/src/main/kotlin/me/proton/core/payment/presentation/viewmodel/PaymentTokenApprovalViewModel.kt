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

package me.proton.core.payment.presentation.viewmodel

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.usecase.GetPaymentTokenStatus
import me.proton.core.payment.presentation.entity.SecureEndpoint
import me.proton.core.presentation.viewmodel.ProtonViewModel
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope

class PaymentTokenApprovalViewModel @ViewModelInject constructor(
    private val getPaymentTokenStatus: GetPaymentTokenStatus,
    private val secureEndpoint: SecureEndpoint,
    private val networkManager: NetworkManager
) : ProtonViewModel(), ViewStateStoreScope {

    val approvalResult = ViewStateStore<State>().lock
    val networkConnection = ViewStateStore<Boolean>().lock

    sealed class State {
        object Processing : State()
        data class Success(val paymentTokenStatus: PaymentTokenStatus) : State()
        sealed class Error : State() {
            data class Message(val message: String?) : Error()
        }
    }

    /**
     * Handles the Webview redirect result. It also checks if the payment token has been approved.
     */
    fun handleRedirection(
        userId: UserId?,
        paymentToken: String,
        uri: Uri,
        paymentReturnHost: String
    ) =
        if (uri.host == secureEndpoint.host || uri.host == paymentReturnHost) {
            if (uri.getQueryParameter("cancel") == CANCEL_QUERY_PARAM_VALUE) {
                true
            } else {
                checkPaymentTokenApproved(userId, paymentToken)
                false
            }
        } else {
            false
        }

    /**
     * Watches for any network changes and informs the UI for any state change so that it can act
     * accordingly for any network dependent tasks.
     */
    fun watchNetwork() {
        viewModelScope.launch {
            networkManager.observe().collect { status ->
                networkConnection.postData(
                    data = when (status) {
                        NetworkStatus.Metered, NetworkStatus.Unmetered -> true
                        else -> false
                    },
                    dropOnSame = true
                )
            }
        }
    }

    private fun checkPaymentTokenApproved(userId: UserId?, paymentToken: String) = flow {
        emit(State.Processing)
        emit(State.Success(getPaymentTokenStatus(userId, paymentToken).status))
    }.catch {
        approvalResult.post(State.Error.Message(it.message))
    }.onEach {
        approvalResult.post(it)
    }.launchIn(viewModelScope)

    companion object {
        const val CANCEL_QUERY_PARAM_VALUE = "1"
    }
}
