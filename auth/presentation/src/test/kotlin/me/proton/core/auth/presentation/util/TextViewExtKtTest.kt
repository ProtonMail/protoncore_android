package me.proton.core.auth.presentation.util

import android.content.Context
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.widget.TextView
import androidx.core.text.getSpans
import androidx.test.core.app.ApplicationProvider
import me.proton.core.auth.presentation.R
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class TextViewExtKtTest {
    private lateinit var context: Context
    private lateinit var textView: TextView

    private val annotatedText = "By using this app you accept our Terms and Conditions."
    private val notAnnotatedText = "Accept our Terms and Conditions"

    @BeforeTest
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        textView = TextView(context)
    }

    @Test
    fun `annotated link is clickable`() {
        // GIVEN
        var clicked = false
        textView.setTextWithAnnotatedLink(R.string.auth_credentialless_terms, "terms") {
            clicked = true
        }
        val spannableString = textView.text as SpannableString
        val clickableSpan = requireNotNull(spannableString.getSpans<ClickableSpan>().firstOrNull())

        // WHEN
        clickableSpan.onClick(textView)

        // THEN
        assertTrue(clicked)
        assertEquals(annotatedText, textView.text.toString())
        assertNotEquals(0, spannableString.getSpanStart(clickableSpan))
        assertNotEquals(spannableString.length, spannableString.getSpanEnd(clickableSpan))
    }

    @Test
    fun `whole text view is clickable if no annotation found`() {
        // GIVEN
        textView.setTextWithAnnotatedLink(notAnnotatedText, "terms") {
            // no-op
        }
        val spannableString = textView.text as SpannableString
        val clickableSpan = requireNotNull(spannableString.getSpans<ClickableSpan>().firstOrNull())

        // THEN
        assertEquals(notAnnotatedText, textView.text.toString())
        assertEquals(0, spannableString.getSpanStart(clickableSpan))
        assertEquals(spannableString.length, spannableString.getSpanEnd(clickableSpan))
    }

    @Test
    fun `text view is not clickable if no annotation found and no fallback`() {
        // GIVEN
        textView.setTextWithAnnotatedLink(notAnnotatedText, "terms", fallbackEnabled = false) {
            // no-op
        }
        val spannableString = textView.text as SpannableString
        val clickableSpan = spannableString.getSpans<ClickableSpan>().firstOrNull()

        // THEN
        assertNull(clickableSpan)
        assertEquals(notAnnotatedText, textView.text.toString())
    }
}
