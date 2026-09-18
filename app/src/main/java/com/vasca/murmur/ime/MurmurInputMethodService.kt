package com.vasca.murmur.ime

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.vasca.murmur.data.HistoryRepository
import com.vasca.murmur.dictation.DictionaryManager
import com.vasca.murmur.dictation.DictationController
import com.vasca.murmur.model.ModelInstaller
import com.vasca.murmur.text.TextFormatter

class MurmurInputMethodService : InputMethodService() {
    private val main = Handler(Looper.getMainLooper())
    private lateinit var status: TextView
    private lateinit var transcript: TextView
    private lateinit var waveform: WaveformView
    private lateinit var talk: MaterialButton
    private var controller: DictationController? = null
    override fun onCreateInputView(): View {
        val pad = dp(12)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad); setBackgroundColor(0xff17211e.toInt()) }
        status = TextView(this).apply { text = "MURMUR  •  READY"; setTextColor(0xfff59d3d.toInt()); typeface = Typeface.DEFAULT_BOLD; textSize = 13f }
        waveform = WaveformView(this).apply { layoutParams = LinearLayout.LayoutParams(-1, dp(44)) }
        transcript = TextView(this).apply { text = "Hold TALK and speak"; setTextColor(0xfff4e9c8.toInt()); textSize = 16f; minLines = 2; gravity = Gravity.CENTER_VERTICAL }
        talk = MaterialButton(this).apply { text = "HOLD TO TALK"; isAllCaps = false; textSize = 17f; setOnTouchListener(::onTalkTouch) }
        root.addView(status); root.addView(waveform); root.addView(transcript, LinearLayout.LayoutParams(-1, dp(54))); root.addView(talk, LinearLayout.LayoutParams(-1, dp(54)))
        return root
    }
    private fun onTalkTouch(view: View, event: MotionEvent): Boolean = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> { begin(); true }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { end(); true }
        else -> true
    }
    private fun begin() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { Toast.makeText(this, "Open Murmur and allow microphone access first.", Toast.LENGTH_LONG).show(); return }
        val installer = ModelInstaller(this)
        if (!installer.isInstalled()) { Toast.makeText(this, "Open Murmur and install its offline speech model first.", Toast.LENGTH_LONG).show(); return }
        transcript.text = "Listening…"; status.text = "MURMUR  •  RECORDING"; talk.text = "RELEASE TO INSERT"
        controller?.close()
        controller = DictationController(
            installer.modelDirectory, TextFormatter(DictionaryManager(this)),
            { partial -> main.post { transcript.text = partial.ifBlank { "Listening…" } } },
            { level -> waveform.setLevel(level) },
            { final, duration -> main.post { insertFinal(final, duration) } },
            { message -> main.post { status.text = "MURMUR  •  ERROR"; transcript.text = message; resetButton() } }
        ).also { it.start() }
    }
    private fun end() { controller?.stop(); status.text = "MURMUR  •  TRANSCRIBING"; talk.isEnabled = false }
    private fun insertFinal(text: String, duration: Long) {
        waveform.setLevel(0f); resetButton()
        if (text.isBlank()) { transcript.text = "I didn't catch that. Try again."; status.text = "MURMUR  •  READY"; return }
        currentInputConnection?.commitText(text, 1)
        HistoryRepository(this).add(text, duration, currentInputEditorInfo?.packageName)
        transcript.text = text; status.text = "MURMUR  •  INSERTED"
    }
    private fun resetButton() { talk.isEnabled = true; talk.text = "HOLD TO TALK" }
    override fun onDestroy() { controller?.close(); super.onDestroy() }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
