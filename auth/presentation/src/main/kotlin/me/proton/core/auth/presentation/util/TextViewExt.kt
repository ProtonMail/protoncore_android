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

package me.proton.core.auth.presentation.util

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.text.getSpans

/**
 * Sets a [text] on [this][TextView].
 * The [text] resource can contain a special annotation, for example:
 * `<string name="my_string">Accept our <annotation link="terms">Terms and Conditions</annotation></string>`.
 * Then you can call this method: `setTextWithAnnotatedLink(R.string.my_string, "terms") { openTermsActivity() }`.
 * In case the annotation cannot be found, and if [fallbackEnabled] is true,
 * the whole text will become clickable.
 */
internal fun TextView.setTextWithAnnotatedLink(
    @StringRes text: Int,
    linkAttributeValue: String,
    fallbackEnabled: Boolean = true,
    onLinkClicked: () -> Unit
) {
    setTextWithAnnotatedLink(
        text = context.getText(text),
        linkAttributeValue = linkAttributeValue,
        fallbackEnabled = fallbackEnabled,
        onLinkClicked = onLinkClicked
    )
}

internal fun TextView.setTextWithAnnotatedLink(
    text: CharSequence,
    linkAttributeValue: String,
    fallbackEnabled: Boolean = true,
    onLinkClicked: () -> Unit
) {
    val spannableString = SpannableString(text)
    val annotations = spannableString.getSpans<android.text.Annotation>()
        .filter { it.key == "link" && it.value == linkAttributeValue }
    val linkIndices = annotations.map {
        Pair(
            spannableString.getSpanStart(it),
            spannableString.getSpanEnd(it)
        )
    }
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onLinkClicked()
        }
    }
    if (linkIndices.isNotEmpty()) {
        linkIndices.forEach { (start, end) ->
            spannableString.setSpan(
                clickableSpan,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    } else if (fallbackEnabled) {
        spannableString.setSpan(
            clickableSpan,
            0,
            spannableString.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
    }
    setText(spannableString)
    movementMethod = LinkMovementMethod.getInstance()
}