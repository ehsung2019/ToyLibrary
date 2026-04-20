package com.toylibrary.app.data

import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.toylibrary.app.BuildConfig
import com.toylibrary.app.data.GeminiRepository
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class GeminiRepository {

    val model = GenerativeModel(
        modelName = "models/gemini-3-flash-preview",
        apiKey = BuildConfig.GEMINI_API_KEY
    )





suspend fun generateToyDescription(toyName: String): String {

    val prompt = """
        Write a short friendly toy description for parents in a toy library app.
        Toy name: $toyName

        Include:
        - what the toy does
        - learning benefits
        - recommended age
        - keep it under 2 sentences
    """.trimIndent()

    return askGemini(prompt)
}

    suspend fun askGemini(question: String): String {
        return try {
            val response = model.generateContent(
                content { text(question) }
            )

            response.text ?: "No response from Gemini"
        } catch (e: Exception) {
            e.printStackTrace()
            "Gemini error: ${e.message}"
        }
    }
    suspend fun askGeminiVision(context: Context, prompt: String, uri: Uri): String {
        return try {
            // Convert URI to Bitmap
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return "Failed to read image"
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val response = model.generateContent(
                content {
                    text(prompt)
                    image(bitmap) // ✅ Bitmap now
                }
            )

            response.text ?: "No response from Gemini"
        } catch (e: Exception) {
            e.printStackTrace()
            "Gemini error: ${e.message}"
        }
    }
}


