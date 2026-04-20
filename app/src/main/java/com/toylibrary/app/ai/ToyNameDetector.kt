package com.toylibrary.app.ai

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await

suspend fun detectToyName(context: Context, uri: Uri): String {

    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream) ?: return "Toy"

    val image = InputImage.fromBitmap(bitmap, 0)

    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    val labels = labeler.process(image).await()

    val best = labels.maxByOrNull { it.confidence }

    return best?.text ?: "Toy"
}