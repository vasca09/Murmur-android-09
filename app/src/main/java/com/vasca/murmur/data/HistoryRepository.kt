package com.vasca.murmur.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class HistoryItem(val id: Long, val text: String, val timestamp: Long, val durationMs: Long, val packageName: String?)
class HistoryRepository(context: Context) : SQLiteOpenHelper(context, "murmur-history.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) { db.execSQL("CREATE TABLE history (id INTEGER PRIMARY KEY AUTOINCREMENT, text TEXT NOT NULL, timestamp INTEGER NOT NULL, duration_ms INTEGER NOT NULL, package_name TEXT)") }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    fun add(text: String, durationMs: Long, packageName: String?) { writableDatabase.execSQL("INSERT INTO history(text,timestamp,duration_ms,package_name) VALUES(?,?,?,?)", arrayOf(text, System.currentTimeMillis(), durationMs, packageName)) }
    fun search(query: String = ""): List<HistoryItem> = readableDatabase.rawQuery("SELECT id,text,timestamp,duration_ms,package_name FROM history WHERE text LIKE ? ORDER BY timestamp DESC LIMIT 200", arrayOf("%$query%")).use { cursor ->
        buildList { while (cursor.moveToNext()) add(HistoryItem(cursor.getLong(0), cursor.getString(1), cursor.getLong(2), cursor.getLong(3), cursor.getString(4))) }
    }
    fun clear() { writableDatabase.delete("history", null, null) }
}
