package com.toylibrary.app.ai

import com.toylibrary.app.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

object GeminiService {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY

    )

    suspend fun ask(prompt: String): String {
        return try {
            val response = model.generateContent(prompt)
            response.text ?: "I couldn't generate a response."
        } catch (e: Exception) {
            "Gemini error: ${e.localizedMessage}"
        }
    }
}
