package com.example.voicecalculator

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.*
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import net.objecthunter.exp4j.ExpressionBuilder
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tvResult: TextView
    private lateinit var tvHistory: TextView
    private lateinit var btnVoice: Button
    private lateinit var btnToggleMode: ImageButton
    private lateinit var btnHistory: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var btnToggleScientific: Button
    private lateinit var layoutPhysical: LinearLayout
    private lateinit var layoutScientific: LinearLayout
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var btnSettings: ImageButton
    private lateinit var tvModeText: TextView
    private lateinit var vibrator: Vibrator

    private var currentExpression = ""
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private var isVoiceMode = true
    private var isHistoryVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        isVoiceMode = prefs.getBoolean("isVoiceMode", true)

        tvResult = findViewById(R.id.tvResult)
        tvHistory = findViewById(R.id.tvHistory)
        btnVoice = findViewById(R.id.btnVoice)
        btnToggleMode = findViewById(R.id.btnToggleMode)
        btnHistory = findViewById(R.id.btnHistory)
        btnShare = findViewById(R.id.btnShare)
        btnToggleScientific = findViewById(R.id.btnToggleSci)
        layoutPhysical = findViewById(R.id.layoutPhysical)
        layoutScientific = findViewById(R.id.layoutSci)
        btnSettings = findViewById(R.id.btnSettings)
        tvModeText = findViewById(R.id.tvModeText)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        textToSpeech = TextToSpeech(this, this)

        btnToggleMode.setOnClickListener {
            isVoiceMode = !isVoiceMode
            prefs.edit().putBoolean("isVoiceMode", isVoiceMode).apply()
            updateModeUI()
            vibrate()
        }

        btnVoice.setOnClickListener { startVoiceInput() }
        btnHistory.setOnClickListener {
            isHistoryVisible = !isHistoryVisible
            tvHistory.visibility = if (isHistoryVisible) View.VISIBLE else View.GONE
        }
        btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, tvResult.text.toString())
            startActivity(Intent.createChooser(shareIntent, "Share Calculation"))
        }
        btnToggleScientific.setOnClickListener {
            val isVisible = layoutScientific.visibility == View.VISIBLE
            layoutScientific.visibility = if (isVisible) View.GONE else View.VISIBLE
            btnToggleScientific.text = if (isVisible) "🔬 Show Scientific" else "🔬 Hide Scientific"
        }
        btnSettings.setOnClickListener { showSettingsDialog() }

        val buttons = listOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
            R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7",
            R.id.btn8 to "8", R.id.btn9 to "9", R.id.btnDot to ".",
            R.id.btnAdd to "+", R.id.btnSub to "-", R.id.btnMul to "*", R.id.btnDiv to "/",
            R.id.btnPercent to "%"
        )
        buttons.forEach { (id, symbol) ->
            findViewById<Button>(id).setOnClickListener {
                currentExpression += symbol
                tvResult.text = currentExpression
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            currentExpression = ""
            tvResult.text = ""
        }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener {
            if (currentExpression.isNotEmpty()) {
                currentExpression = currentExpression.dropLast(1)
                tvResult.text = currentExpression
            }
        }
        findViewById<Button>(R.id.btnEqual).setOnClickListener {
            evaluateExpression(currentExpression)
        }

        val sciButtons = listOf("sin", "cos", "tan", "log", "sqrt", "^")
        layoutScientific.removeAllViews()
        sciButtons.forEach { label ->
            val button = Button(this).apply {
                text = label
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(128, 128)
                setOnClickListener {
                    currentExpression += when (label) {
                        "sqrt" -> "sqrt("
                        else -> "$label("
                    }
                    tvResult.text = currentExpression
                }
            }
            layoutScientific.addView(button)
        }

        updateModeUI()
    }

    private fun updateModeUI() {
        if (isVoiceMode) {
            btnVoice.visibility = View.VISIBLE
            layoutPhysical.visibility = View.GONE
            btnToggleScientific.visibility = View.GONE
            btnToggleMode.setImageResource(R.drawable.ic_microphone)
            tvModeText.text = "Mic"
        } else {
            btnVoice.visibility = View.GONE
            layoutPhysical.visibility = View.VISIBLE
            btnToggleScientific.visibility = View.VISIBLE
            btnToggleMode.setImageResource(R.drawable.ic_keyboard)
            tvModeText.text = "Keyboard"
        }
        tvResult.text = ""
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your calculation")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        try {
            Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show()
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            tvResult.text = "Error: ${e.message}"
            speakText("Sorry, I couldn't start voice recognition.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""
            tvResult.text = spokenText
            evaluateExpression(spokenText)
        }
    }

    private fun evaluateExpression(expression: String) {
        val parsed = MathTranslator.translateToMath(expression, "en")
        try {
            val result = ExpressionBuilder(parsed).build().evaluate()
            val response = "$expression = $result"
            tvResult.text = response
            speakText(result.toString())
            tvHistory.append("\n$response")
            currentExpression = result.toString()
        } catch (e: Exception) {
            tvResult.text = "Could not evaluate"
            speakText("Sorry, I couldn't evaluate the expression.")
        }
    }

    private fun speakText(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(50)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.getDefault()
        }
    }

    override fun onDestroy() {
        textToSpeech.shutdown()
        super.onDestroy()
    }

    private fun showSettingsDialog() {
        val themes = arrayOf("Light", "Dark", "High Contrast")
        val languages = arrayOf("English", "Hindi", "Marathi", "Gujarati", "Tamil", "Telugu", "Kannada", "Bengali")
        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(arrayOf("Select Theme", "Choose Language", "About App")) { _, which ->
                when (which) {
                    0 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Choose Theme")
                            .setItems(themes) { _, index ->
                                when (themes[index]) {
                                    "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                    "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                    else -> Toast.makeText(this, "High Contrast coming soon", Toast.LENGTH_SHORT).show()
                                }
                            }.show()
                    }
                    1 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Choose Language")
                            .setItems(languages) { _, index ->
                                Toast.makeText(this, "Language set to ${languages[index]}", Toast.LENGTH_SHORT).show()
                            }.show()
                    }
                    2 -> {
                        AlertDialog.Builder(this)
                            .setTitle("About App")
                            .setMessage("Voice Calculator with scientific support and multilingual UI. Built in Kotlin by MR. Suya.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }.setNegativeButton("Cancel", null)
            .show()
    }
}
