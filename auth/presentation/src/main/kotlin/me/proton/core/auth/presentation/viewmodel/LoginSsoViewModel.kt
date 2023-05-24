package me.proton.core.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.usecase.CreateLoginSsoSession
import me.proton.core.auth.domain.usecase.GetAuthInfo
import me.proton.core.auth.presentation.LogTag
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.util.kotlin.catchAll
import me.proton.core.util.kotlin.catchWhen
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import javax.inject.Inject

@HiltViewModel
class LoginSsoViewModel @Inject constructor(
    private val getAuthInfo: GetAuthInfo,
    private val createLoginSsoSession: CreateLoginSsoSession
) : ViewModel() {

    private val mutableState = MutableStateFlow<State>(State.Idle)
    val state = mutableState.asStateFlow()

    sealed class State {
        object Idle : State()
        object Processing : State()
        data class SignInWithSrp(val error: Throwable) : State()
        data class StartToken(val token: String) : State()
        data class Success(val userId: UserId) : State()
        data class Error(val error: Throwable) : State()
    }

    fun startLoginWorkflow(
        email: String,
    ) = viewModelScope.launchWithResultContext {
        onResult("getAuthInfo") { }
        flow {
            emit(State.Processing)
            // TODO: Add Intent to AuthInfo call.
            // TODO: Get Token from getAuthInfo.
            getAuthInfo(sessionId = null, username = email)
            emit(State.StartToken("tokenFromAuthInfoCall"))
        }.catchWhen(Throwable::isSwitchToSrp) {
            emit(State.SignInWithSrp(it))
        }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
            emit(State.Error(it))
        }.onEach { state ->
            mutableState.tryEmit(state)
        }.collect()
    }

    fun createSessionWithToken(
        email: String,
        token: String,
        requiredAccountType: AccountType
    ) = viewModelScope.launchWithResultContext {
        onResult("performLogin") { }
        flow {
            emit(State.Processing)
            val sessionInfo = createLoginSsoSession(email, token, requiredAccountType)
            emit(State.Success(sessionInfo.userId))
        }.catchWhen(Throwable::isSwitchToSrp) {
            emit(State.SignInWithSrp(it))
        }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
            emit(State.Error(it))
        }.onEach { state ->
            mutableState.tryEmit(state)
        }.collect()
    }
}

private fun Throwable.isSwitchToSrp(): Boolean {
    if (this !is ApiException) return false
    val error = error as? ApiResult.Error.Http
    return error?.proton?.code == ResponseCodes.AUTH_SWITCH_TO_SRP
}
