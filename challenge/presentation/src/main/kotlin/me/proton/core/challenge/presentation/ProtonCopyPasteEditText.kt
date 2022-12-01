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

package me.proton.core.challenge.presentation

import android.content.ClipDescription
import android.content.ClipDescription.MIMETYPE_TEXT_HTML
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText

/**
 * TextInputEditText that notifies the copy and paste events for anti abuse purposes.
 */
public class ProtonCopyPasteEditText : TextInputEditText {

    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private var copyPasteListener: OnCopyPasteListener? = null

    public constructor(
        context: Context
    ) : super(context)

    public constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(context, attrs)

    public constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle)

    public fun setOnCopyPasteListener(listener: OnCopyPasteListener) {
        copyPasteListener = listener
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        val consumed = super.onTextContextMenuItem(id)
        when (id) {
            android.R.id.copy -> onCopy()
            android.R.id.cut -> onCopy()
            android.R.id.paste -> onPaste()
        }
        return consumed
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        copyPasteListener?.onFocus(focused)
    }

    private fun onCopy() {
        getTextFromClipboard()?.let { copyPasteListener?.onCopyText(it) }
    }

    private fun onPaste() {
        getTextFromClipboard()?.let { copyPasteListener?.onPasteText(it) }
    }

    private val primaryClip
        get() = clipboard.primaryClip?.getItemAt(0)?.text?.toString()

    private val primaryClipDescription
        get() = clipboard.primaryClipDescription

    private fun ClipDescription?.hasText() = when {
        this == null -> false
        hasMimeType(MIMETYPE_TEXT_PLAIN) -> true
        hasMimeType(MIMETYPE_TEXT_HTML) -> true
        else -> false
    }

    private fun getTextFromClipboard(): String? = when {
        !clipboard.hasPrimaryClip() -> null
        primaryClipDescription.hasText() -> primaryClip
        else -> null
    }

    public interface OnCopyPasteListener {
        public fun onCopyText(text: String)
        public fun onPasteText(text: String)
        public fun onFocus(focused: Boolean)
    }
}
