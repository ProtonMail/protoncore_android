package me.proton.core.util.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals

class StringUtilsKtTest {
    @Test
    fun `encoding URI path segments`() {
        assertEquals("", "".toEncodedUriPathSegment())
        assertEquals("%2F", "/".toEncodedUriPathSegment())
        assertEquals("test", "test".toEncodedUriPathSegment())
        assertEquals("test%201", "test 1".toEncodedUriPathSegment())
        assertEquals("test%2F", "test/".toEncodedUriPathSegment())
        assertEquals("test%0A", "test\n".toEncodedUriPathSegment())
        assertEquals("foo%20+bar", "foo +bar".toEncodedUriPathSegment())
    }
}
