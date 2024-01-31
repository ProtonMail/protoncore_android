/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.plan.presentation.view

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import coil.request.ImageRequest

internal object EntitlementImageLoader {

    private var imageLoader: ImageLoader? = null

    private fun getImageLoader(context: Context) = imageLoader ?: ImageLoader.Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .build()
        .apply { imageLoader = this }

    internal fun ImageView.loadIcon(
        data: Any?,
        context: Context,
        fallback: ((ImageRequest.Builder) -> Unit)? = null
    ) {
        load(data, getImageLoader(context)) {
            if (fallback != null) {
                fallback(this)
            }
        }
    }
}
