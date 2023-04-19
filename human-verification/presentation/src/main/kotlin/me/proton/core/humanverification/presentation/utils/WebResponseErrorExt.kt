package me.proton.core.humanverification.presentation.utils

import me.proton.core.humanverification.presentation.ui.common.WebResponseError
import me.proton.core.observability.domain.metrics.HvPageLoadTotal

@Suppress("MagicNumber")
internal fun WebResponseError?.toHvPageLoadStatus() = when (this) {
    is WebResponseError.Http -> when (response.statusCode) {
        in 200..299 -> HvPageLoadTotal.Status.http2xx
        400 -> HvPageLoadTotal.Status.http400
        404 -> HvPageLoadTotal.Status.http404
        422 -> HvPageLoadTotal.Status.http422
        in 400..499 -> HvPageLoadTotal.Status.http4xx
        in 500..599 -> HvPageLoadTotal.Status.http5xx
        else -> HvPageLoadTotal.Status.connectionError
    }
    is WebResponseError.Ssl -> HvPageLoadTotal.Status.sslError
    is WebResponseError.Resource -> HvPageLoadTotal.Status.connectionError
    else -> HvPageLoadTotal.Status.connectionError
}
