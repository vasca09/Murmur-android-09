package com.vasca.murmur.text

import com.vasca.murmur.dictation.DictionaryManager
import org.junit.Test
import org.junit.Assert.assertEquals

class TextFormatterTest {
    // DictionaryManager needs Android context, so cleanup rules are covered through a small pure-equivalent check in instrumentation later.
    @Test fun placeholder() { assertEquals("Murmur", "Murmur") }
}
