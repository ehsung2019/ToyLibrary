package com.toylibrary.app.ai

import android.content.Context
import android.net.Uri
import com.toylibrary.app.data.GeminiRepository

data class ToyInfo(
    val name: String,
    val age: String,
    val description: String
)

suspend fun detectToyInfo(
    context: Context,
    uri: Uri
): ToyInfo {

    val repository = GeminiRepository()

    val prompt = """
Analyze this toy image.

Return EXACTLY in this format:

Toy Name: <name>
Age Range: <age>
Description: <short description under 40 words>

Age format examples: 1+, 3+, 5+, 8+
""".trimIndent()

    // Pass context as first argument
    val aiReply = repository.askGeminiVision(context, prompt, uri)

    if (aiReply.contains("error")) {
        return ToyInfo(
            name = "Unknown Toy",
            age = "3+",
            description = "AI could not analyze this toy."
        )
    }

    val lines = aiReply.lines()

    val name = lines.getOrNull(0)?.replace("Toy Name:", "")?.trim() ?: "Toy"
    val age = lines.getOrNull(1)?.replace("Age Range:", "")?.trim() ?: "3+"
    val description = lines.getOrNull(2)?.replace("Description:", "")?.trim() ?: ""

    return ToyInfo(name, age, description)
}