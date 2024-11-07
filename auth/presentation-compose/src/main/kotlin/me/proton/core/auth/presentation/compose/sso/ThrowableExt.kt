package me.proton.core.auth.presentation.compose.sso

import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes

public fun Throwable.isActionNotAllowed(): Boolean {
    if (this !is ApiException) return false
    val error = error as? ApiResult.Error.Http
    return error?.proton?.code == ResponseCodes.NOT_ALLOWED
}

public fun Throwable.isMissingScope(): Boolean {
    if (this !is ApiException) return false
    val error = error as? ApiResult.Error.Http
    return error?.proton?.code in listOf(ResponseCodes.SCOPE_REAUTH_LOCKED, ResponseCodes.SCOPE_REAUTH_PASSWORD)
}
