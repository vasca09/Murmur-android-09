package com.vasca.murmur.dictation

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class DictionaryEntry(val spoken: String, val replacement: String)
class DictionaryManager(context: Context) {
    private val prefs = context.getSharedPreferences("dictionary", Context.MODE_PRIVATE)
    fun entries(): List<DictionaryEntry> = runCatching {
        val values = JSONArray(prefs.getString("entries", "[]"))
        List(values.length()) { index -> values.getJSONObject(index).let { DictionaryEntry(it.getString("spoken"), it.getString("replacement")) } }
    }.getOrDefault(emptyList())
    fun save(entries: List<DictionaryEntry>) {
        val array = JSONArray(); entries.forEach { array.put(JSONObject().put("spoken", it.spoken).put("replacement", it.replacement)) }
        prefs.edit().putString("entries", array.toString()).apply()
    }
    fun apply(text: String): String = entries().sortedByDescending { it.spoken.length }.fold(text) { output, entry ->
        output.replace(Regex("(?i)(?<![\\p{L}\\p{N}])${Regex.escape(entry.spoken)}(?![\\p{L}\\p{N}])"), entry.replacement)
    }
}
