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

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText

/**
 * TextInputEditText that notifies the copy and paste events for anti abuse purposes.
 */
class ProtonCopyPasteEditText : TextInputEditText {

    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    internal val copyList: MutableList<String> = mutableListOf()
    internal val pasteList: MutableList<String> = mutableListOf()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onTextContextMenuItem(id: Int): Boolean {
        val consumed = super.onTextContextMenuItem(id)
        when (id) {
            android.R.id.copy,
            android.R.id.cut -> onCopy()
            android.R.id.paste -> onPaste()
        }
        return consumed
    }

    private fun onCopy() = checkClipboardAndAddToList(copyList)

    private fun onPaste() = checkClipboardAndAddToList(pasteList)

    private fun checkClipboardAndAddToList(list: MutableList<String>) {
        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) == true) {
            val item = clipboard.primaryClip?.getItemAt(0)
            list.add(item?.text.toString())
        }
    }
}
