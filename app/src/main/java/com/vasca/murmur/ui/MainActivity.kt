package com.vasca.murmur.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.vasca.murmur.data.HistoryRepository
import com.vasca.murmur.dictation.DictionaryEntry
import com.vasca.murmur.dictation.DictionaryManager
import com.vasca.murmur.model.ModelInstaller
import java.text.DateFormat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var installer: ModelInstaller
    private lateinit var state: TextView
    private val requestMic = registerForActivityResult(ActivityResultContracts.RequestPermission()) { render() }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); installer = ModelInstaller(this); render() }
    override fun onResume() { super.onResume(); if (::installer.isInitialized) render() }

    private fun render() {
        val padding = dp(20)
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(padding, padding, padding, padding); setBackgroundColor(0xff17211e.toInt()) }
        page.addView(TextView(this).apply { text = "MURMUR"; setTextColor(0xfff4e9c8.toInt()); textSize = 32f; typeface = android.graphics.Typeface.DEFAULT_BOLD })
        page.addView(TextView(this).apply { text = "Offline voice typing"; setTextColor(0xfff59d3d.toInt()); textSize = 16f })
        state = TextView(this).apply { setTextColor(0xfff4e9c8.toInt()); textSize = 16f; setPadding(0, dp(28), 0, dp(20)); text = setupState() }
        page.addView(state)
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) page.addView(button("Allow microphone") { requestMic.launch(Manifest.permission.RECORD_AUDIO) })
        if (!installer.isInstalled()) page.addView(button("Install offline speech model (40 MB)") { installModel() })
        page.addView(button("Enable Murmur keyboard") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) })
        page.addView(button("Choose Murmur keyboard") { (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() })
        page.addView(button("Personal dictionary") { dictionaryDialog() })
        page.addView(button("Transcription history") { historyDialog() })
        page.addView(TextView(this).apply { text = "Your microphone audio stays in memory and is discarded after transcription."; setTextColor(0xffb7c5b9.toInt()); textSize = 13f; setPadding(0, dp(24), 0, 0) })
        setContentView(ScrollView(this).apply { addView(page) })
    }
    private fun setupState(): String = when {
        !installer.isInstalled() -> "SETUP  1/3  Install the one offline English speech model."
        checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED -> "SETUP  2/3  Allow microphone access."
        else -> "READY  Enable Murmur, select it in any text field, then hold TALK."
    }
    private fun installModel() {
        state.text = "Installing the one offline speech model…"
        Executors.newSingleThreadExecutor().execute {
            runCatching { installer.install { progress -> runOnUiThread { state.text = "Installing offline speech model… $progress%" } } }
                .onSuccess { runOnUiThread { render() } }
                .onFailure { error -> runOnUiThread { state.text = "Installation failed: ${error.message}" } }
        }
    }
    private fun dictionaryDialog() {
        val manager = DictionaryManager(this); val entries = manager.entries().toMutableList()
        fun show() {
            val labels = entries.map { "${it.spoken}  →  ${it.replacement}" }.toTypedArray()
            AlertDialog.Builder(this).setTitle("Personal dictionary").setItems(labels) { _, which -> entries.removeAt(which); manager.save(entries); show() }
                .setPositiveButton("Add") { _, _ -> addDictionaryEntry(entries, manager) }.setNegativeButton("Close", null).show()
        }; show()
    }
    private fun addDictionaryEntry(entries: MutableList<DictionaryEntry>, manager: DictionaryManager) {
        val spoken = EditText(this).apply { hint = "What you say, e.g. open eye" }; val replacement = EditText(this).apply { hint = "Insert as, e.g. OpenAI" }
        AlertDialog.Builder(this).setTitle("Add replacement").setView(LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), 0, dp(20), 0); addView(spoken); addView(replacement) })
            .setPositiveButton("Save") { _, _ -> if (spoken.text.isNotBlank() && replacement.text.isNotBlank()) { entries.add(DictionaryEntry(spoken.text.toString().trim(), replacement.text.toString().trim())); manager.save(entries) } }.setNegativeButton("Cancel", null).show()
    }
    private fun historyDialog() {
        val repo = HistoryRepository(this); val items = repo.search()
        val text = items.joinToString("\n\n") { "${DateFormat.getDateTimeInstance().format(it.timestamp)}\n${it.text}" }.ifBlank { "No transcriptions yet." }
        AlertDialog.Builder(this).setTitle("History").setMessage(text).setPositiveButton("Clear") { _, _ -> repo.clear() }.setNegativeButton("Close", null).show()
    }
    private fun button(label: String, action: () -> Unit) = MaterialButton(this).apply { text = label; isAllCaps = false; gravity = Gravity.CENTER; setOnClickListener { action() }; layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) } }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
