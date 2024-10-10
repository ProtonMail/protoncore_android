package me.proton.core.auth.presentation.compose.sso

import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes

public fun Throwable.isActionNotAllowed(): Boolean {
    if (this !is ApiException) return false
    val error = error as? ApiResult.Error.Http
    return error?.proton?.code == ResponseCodes.NOT_ALLOWED
}
