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

package me.proton.core.plan.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.metrics.CheckoutBillingSubscribeTotalV1
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.payment.domain.entity.toCheckoutBillingSubscribeManager
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchase
import me.proton.core.plan.presentation.usecase.CheckUnredeemedGooglePurchase
import me.proton.core.plan.presentation.usecase.RedeemGooglePurchase
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
internal class UnredeemedPurchaseViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val checkUnredeemedGooglePurchase: CheckUnredeemedGooglePurchase,
    private val redeemGooglePurchase: RedeemGooglePurchase
) : ProtonViewModel() {
    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    internal val state: StateFlow<State> = _state.asStateFlow()

    init {
        performCheck()
    }

    internal fun redeemPurchase(
        unredeemedPurchase: UnredeemedGooglePurchase,
        userId: UserId
    ) = flow {
        emit(State.Loading)
        redeemGooglePurchase(
            unredeemedPurchase.googlePurchase,
            unredeemedPurchase.purchasedPlan,
            unredeemedPurchase.status,
            userId,
            subscribeMetricData = { result, management ->
                CheckoutBillingSubscribeTotalV1(
                    result.toHttpApiStatus(),
                    management.toCheckoutBillingSubscribeManager()
                )
            }
        )
        emit(State.PurchaseRedeemed)
    }.catch {
        _state.emit(State.Error)
    }.onEach {
        _state.emit(it)
    }.launchIn(viewModelScope)

    private fun performCheck() = flow {
        emit(State.Loading)
        val userId = accountManager.getPrimaryUserId().first() ?: return@flow
        val unredeemed = checkUnredeemedGooglePurchase.invoke(userId)
        if (unredeemed != null) {
            emit(State.UnredeemedPurchase(unredeemed, userId))
        } else {
            emit(State.NoUnredeemedPurchases)
        }
    }.catch {
        _state.emit(State.Error)
    }.onEach {
        _state.emit(it)
    }.launchIn(viewModelScope)

    internal sealed class State {
        object Loading : State()
        object Error : State()
        class UnredeemedPurchase(
            val unredeemedPurchase: UnredeemedGooglePurchase,
            val userId: UserId
        ) : State()

        object NoUnredeemedPurchases : State()
        object PurchaseRedeemed : State()
    }
}
