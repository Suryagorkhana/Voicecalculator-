package com.example.voicecalculator

object MathTranslator {

    private val mathTranslations = mapOf(
        "hi" to mapOf(
            "जोड़ो" to "+", "घटाओ" to "-", "गुणा" to "*", "भाग" to "/",
            "पॉइंट" to ".", "बटा" to "/", "प्लस" to "+", "माइनस" to "-",
            "इनटू" to "*", "डिवाइड" to "/", "घात" to "^", "ओपन" to "(", "क्लोज" to ")"
        ),
        "mr" to mapOf(
            "बेरीज" to "+", "वजा" to "-", "गुणा" to "*", "भाग" to "/",
            "पूर्णांक" to ".", "घात" to "^", "ओपन" to "(", "क्लोज" to ")"
        ),
        "gu" to mapOf(
            "ઉમેરો" to "+", "બાકી કરો" to "-", "ગુણાકાર" to "*", "ભાગાકાર" to "/",
            "બિંદુ" to ".", "પાવર" to "^", "ખુલ્લું કૌંસ" to "(", "બંધ કૌંસ" to ")"
        ),
        "ta" to mapOf(
            "கூட்டு" to "+", "கழித்தல்" to "-", "பெருக்கல்" to "*", "வகுத்தல்" to "/",
            "புள்ளி" to ".", "அதிகாரம்" to "^", "திறந்த அடைப்பு" to "(", "மூடிய அடைப்பு" to ")"
        ),
        "te" to mapOf(
            "జోడించు" to "+", "తగ్గించు" to "-", "గుణించు" to "*", "భాగించు" to "/",
            "పాయింట్" to ".", "పవర్" to "^", "తెరవు" to "(", "మూసివేయి" to ")"
        ),
        "kn" to mapOf(
            "ಸೇರಿಸು" to "+", "ಕಡಿಮೆಮಾಡಿ" to "-", "ಗುಣಿಸು" to "*", "ಭಾಗಿಸು" to "/",
            "ಬಿಂದು" to ".", "ಶಕ್ತಿ" to "^", "ತೆರೆಯಿರಿ" to "(", "ಮುಚ್ಚಿರಿ" to ")"
        ),
        "bn" to mapOf(
            "যোগ" to "+", "বিয়োগ" to "-", "গুণ" to "*", "ভাগ" to "/",
            "দশমিক" to ".", "ঘাত" to "^", "খুলুন" to "(", "বন্ধ করুন" to ")"
        ),
        "en" to mapOf(
            "plus" to "+", "minus" to "-", "into" to "*", "times" to "*",
            "divide" to "/", "by" to "/", "point" to ".", "x" to "*",
            "power" to "^", "open bracket" to "(", "close bracket" to ")"
        )
    )

    fun translateToMath(expression: String, langCode: String): String {
        val translationMap = mathTranslations[langCode] ?: mathTranslations["en"]!!
        var parsed = expression.lowercase().trim()

        // Sort keywords to replace longer ones first
        val sortedMap = translationMap.toList().sortedByDescending { it.first.length }

        sortedMap.forEach { (keyword, symbol) ->
            parsed = parsed.replace(keyword, symbol, ignoreCase = true)
        }

        return parsed.replace("\\s+".toRegex(), "")
    }
}
