package me.proton.core.challenge.presentation

import android.text.Editable
import android.text.TextWatcher

internal class ProtonMetadataInputWatcher(
    private val keyList: MutableList<String>,
    private val pasteList: MutableList<String>
) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable) {}
    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        val diffCount = count - before
        when {
            diffCount == 0 -> Unit
            diffCount > 1 -> {
                val range = IntRange(start, start + count - 1)
                val newText = text.substring(range)
                keyList.add(ProtonMetadataInput.PASTE)
                pasteList.add(newText)
            }
            diffCount < 0 -> {
                keyList.add(ProtonMetadataInput.BACKSPACE)
            }
            // diffCount == 1
            else -> {
                val textDiff = text.substring(start + before until start + count)
                keyList.add(textDiff)
            }
        }
    }
}
