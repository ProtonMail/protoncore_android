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

package me.proton.core.compose.util

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans

/**
 * @return An [AnnotatedString] where all the [arguments][args] are displayed as bold.
 */
@Suppress("SpreadOperator")
fun String.formatBold(vararg args: Any?): AnnotatedString {
    val html = String.format(this, *args.map { "<b>$it</b>" }.toTypedArray())
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toAnnotatedString()
}

private fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    val spanned = this@toAnnotatedString
    append(spanned.toString())

    getSpans<StyleSpan>().forEach { span ->
        when (span.style) {
            Typeface.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
            // Handle other styles as needed.
            else -> null
        }?.let { addStyle(it, getSpanStart(span), getSpanEnd(span)) }
    }
}
