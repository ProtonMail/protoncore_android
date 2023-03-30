package me.proton.core.challenge.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.KeyEvent
import android.widget.EditText
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class ProtonMetadataInputWatcherTest {
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var context: Context
    private lateinit var editText: EditText
    private lateinit var keyList: MutableList<String>
    private lateinit var pasteList: MutableList<String>

    @BeforeTest
    fun setUp() {
        keyList = mutableListOf()
        pasteList = mutableListOf()
        context = ApplicationProvider.getApplicationContext()
        clipboardManager = context.getSystemService()!!
        editText = EditText(context)
        editText.addTextChangedListener(ProtonMetadataInputWatcher(keyList, pasteList))
    }

    @Test
    fun `type single character`() {
        editText.type("a")

        assertEquals("a", editText.text.toString())
        assertContentEquals(listOf("a"), keyList)
        assertContentEquals(listOf(), pasteList)
    }

    @Test
    fun `type single character in the middle`() {
        editText.type("ab")
        editText.setSelection(1)
        editText.type("c")

        assertEquals("acb", editText.text.toString())
        assertContentEquals(listOf("a", "b", "c"), keyList)
        assertContentEquals(listOf(), pasteList)
    }

    @Test
    fun `type multiple characters`() {
        editText.type("ab c")

        assertEquals("ab c", editText.text.toString())
        assertContentEquals(listOf("a", "b", " ", "c"), keyList)
        assertContentEquals(listOf(), pasteList)
    }

    @Test
    fun `remove last character`() {
        editText.type("ab")
        editText.del()
        editText.type("c")

        assertEquals("ac", editText.text.toString())
        assertContentEquals(listOf("a", "b", "Backspace", "c"), keyList)
        assertContentEquals(listOf(), pasteList)
    }

    @Test
    fun `select and replace a character`() {
        editText.type("ab")
        editText.setSelection(0, 1)
        editText.type("c")

        assertEquals("cb", editText.text.toString())
        assertContentEquals(listOf("a", "b"), keyList)
        assertContentEquals(listOf(), pasteList)
    }

    @Test
    fun `select and replace multiple characters`() {
        editText.type("ab")
        editText.setSelection(0, 2)
        editText.type("c")

        assertEquals("c", editText.text.toString())
        assertContentEquals(listOf("a", "b", "Backspace"), keyList)
        assertContentEquals(listOf(), pasteList)
    }

    @Test
    fun `paste from clipboard`() {
        editText.pasteFromClipboard("abc")

        assertEquals("abc", editText.text.toString())
        assertContentEquals(listOf("Paste"), keyList)
        assertContentEquals(listOf("abc"), pasteList)
    }

    @Test
    fun `type text then append by pasting from clipboard`() {
        editText.type("abc ")
        editText.pasteFromClipboard("def")

        assertEquals("abc def", editText.text.toString())
        assertContentEquals(listOf("a", "b", "c", " ", "Paste"), keyList)
        assertContentEquals(listOf("def"), pasteList)
    }

    @Test
    fun `set text at once, into empty text field`() {
        editText.setText("abc")
        assertEquals("abc", editText.text.toString())
        assertContentEquals(listOf("Paste"), keyList)
        assertContentEquals(listOf("abc"), pasteList)
    }

    @Test
    fun `set text at once, at the beginning`() {
        editText.type("abc")
        editText.setSelection(0)

        editText.setText("def ")

        assertEquals("def ", editText.text.toString())
        assertContentEquals(listOf("a", "b", "c", " "), keyList)
        assertContentEquals(listOf(), pasteList)
    }

    @Test
    fun `set text at once, in the middle`() {
        editText.type("ab")
        editText.setSelection(1)

        editText.setText("cde fg")

        assertEquals("cde fg", editText.text.toString())
        assertContentEquals(listOf("a", "b", "Paste"), keyList)
        assertContentEquals(listOf("cde fg"), pasteList)
    }

    @Test
    fun `delete characters`() {
        editText.setText("abc")
        editText.selectAll()
        editText.del()

        assertEquals("", editText.text.toString())
        assertContentEquals(listOf("Paste", "Backspace"), keyList)
        assertContentEquals(listOf("abc"), pasteList)
    }

    @Test
    fun `paste html`() {
        clipboardManager.setPrimaryClip(
            ClipData.newHtmlText(
                "test-label",
                "plain-abc",
                "<a href='#'>abc</a>"
            )
        )
        editText.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PASTE))

        assertEquals("abc", editText.text.toString())
        assertContentEquals(listOf("Paste"), keyList)
        assertContentEquals(listOf("abc"), pasteList)
    }

    /** Type the backspace character. */
    private fun EditText.del() {
        dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
    }

    /** Paste [text] from clipboard into the [this]. */
    private fun EditText.pasteFromClipboard(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("test", text))
        dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PASTE))
    }

    /** Type [text] into the [this]. Only supports ASCII alphanumerical characters. */
    private fun EditText.type(text: String) {
        require(text.matches(Regex("[a-z\\d ]+")))
        text.forEach {
            val keyCode = when {
                it == ' ' -> KeyEvent.KEYCODE_SPACE
                it.isDigit() -> KeyEvent.KEYCODE_0 + it.digitToInt()
                else -> KeyEvent.KEYCODE_A + (it.code - 'a'.code)
            }
            dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        }
    }
}
