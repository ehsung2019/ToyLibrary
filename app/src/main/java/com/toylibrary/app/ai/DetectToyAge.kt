package com.toylibrary.app.ai

import android.content.Context
import android.net.Uri

suspend fun detectToyAge(context: Context, uri: Uri): String {

    // For now we return a simple guess
    // Later this can call Gemini Vision API

    return "3+"
}