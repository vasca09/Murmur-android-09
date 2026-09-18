package com.vasca.murmur.text

import com.vasca.murmur.dictation.DictionaryManager

class TextFormatter(private val dictionary: DictionaryManager) {
    fun format(raw: String): String {
        var text = raw.trim().replace(Regex("\\s+"), " ")
        text = text.replace(Regex("(?i)\\b(um+|uh+|erm)\\b\\s*"), "")
        text = text.replace(Regex("(?i)\\b(\\p{L}+)(\\s+\\1\\b)+"), "$1")
        text = dictionary.apply(text).trim().replace(Regex("\\s+"), " ")
        if (text.isNotEmpty()) text = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        return text
    }
}
