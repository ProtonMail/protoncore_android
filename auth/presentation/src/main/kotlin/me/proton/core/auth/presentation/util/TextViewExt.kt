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
 *
 * The [text] resource can contain a special link annotation, for example:
 * ```
 * <string name="my_string">Accept our <annotation link="terms">Terms and Conditions</annotation></string>.
 * ```
 * Then you can call this method:
 * ```
 * setTextWithAnnotatedLink(R.string.my_string, "terms") { openTermsActivity() }
 * ```
 */
internal fun TextView.setTextWithAnnotatedLink(
    @StringRes text: Int,
    linkAttributeValue: String,
    onLinkClicked: () -> Unit
) = setTextWithAnnotatedLink(text) { link ->
    if (link == linkAttributeValue) {
        onLinkClicked()
    }
}

/**
 * Sets a [text] on [this][TextView].
 *
 * The [text] can contain a special link annotation, for example:
 * ```
 * <string name="my_string">Accept our <annotation link="terms">Terms and Conditions</annotation></string>.
 * ```
 * Then you can call this method:
 * ```
 * setTextWithAnnotatedLink(R.string.my_string, "terms") { openTermsActivity() }
 * ```
 */
internal fun TextView.setTextWithAnnotatedLink(
    text: CharSequence,
    linkAttributeValue: String,
    onLinkClicked: () -> Unit
) = setTextWithAnnotatedLink(text) { link ->
    if (link == linkAttributeValue) {
        onLinkClicked()
    }
}

/**
 * Sets a [text] on [this][TextView].
 *
 * The [text] resource can contain a special link annotation, for example:
 * ```
 * <string name="my_string">Accept our <annotation link="terms">Terms and Conditions</annotation></string>.
 * ```
 * Then you can call this method:
 * ```
 * setTextWithAnnotatedLink(R.string.my_string) { link -> if (link == "terms") openTermsActivity() }
 * ```
 */
internal fun TextView.setTextWithAnnotatedLink(
    @StringRes text: Int,
    onLinkClicked: (link: String) -> Unit
) {
    setTextWithAnnotatedLink(
        text = context.getText(text),
        onLinkClicked = onLinkClicked
    )
}

/**
 * Sets a [text] on [this][TextView].
 *
 * The [text] can contain a special link annotation, for example:
 * ```
 * <string name="my_string">Accept our <annotation link="terms">Terms and Conditions</annotation></string>.
 * ```
 * Then you can call this method:
 * ```
 * setTextWithAnnotatedLink(R.string.my_string) { link -> if (link == "terms") openTermsActivity() }
 * ```
 */
internal fun TextView.setTextWithAnnotatedLink(
    text: CharSequence,
    onLinkClicked: (link: String) -> Unit
) {
    class LinkClickableSpan(
        val key: String,
        val spanStart: Int,
        val spanEnd: Int,
    ) : ClickableSpan() {
        override fun onClick(widget: View) {
            onLinkClicked(key)
        }
    }

    val spannableString = SpannableString(text)
    val linkClickableSpans = spannableString.getSpans<android.text.Annotation>()
        .filter { it.key == "link" }
        .map {
            LinkClickableSpan(
                it.value,
                spannableString.getSpanStart(it),
                spannableString.getSpanEnd(it)
            )
        }

    linkClickableSpans.forEach { linkClickableSpan ->
        spannableString.setSpan(
            linkClickableSpan,
            linkClickableSpan.spanStart,
            linkClickableSpan.spanEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    setText(spannableString)
    movementMethod = LinkMovementMethod.getInstance()
}
