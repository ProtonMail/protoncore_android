package me.proton.core.humanverification.presentation.utils

import me.proton.core.humanverification.presentation.ui.common.WebResponseError
import me.proton.core.observability.domain.metrics.HvPageLoadTotalV1

@Suppress("MagicNumber")
internal fun WebResponseError?.toHvPageLoadStatus() = when (this) {
    is WebResponseError.Http -> when (response.statusCode) {
        in 200..299 -> HvPageLoadTotalV1.Status.http2xx
        in 400..499 -> HvPageLoadTotalV1.Status.http4xx
        in 500..599 -> HvPageLoadTotalV1.Status.http5xx
        else -> HvPageLoadTotalV1.Status.connectionError
    }
    is WebResponseError.Ssl -> HvPageLoadTotalV1.Status.sslError
    is WebResponseError.Resource -> HvPageLoadTotalV1.Status.connectionError
    else -> HvPageLoadTotalV1.Status.connectionError
}
