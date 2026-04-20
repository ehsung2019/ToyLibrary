package com.toylibrary.app.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.google.mlkit.vision.segmentation.Segmentation
import kotlinx.coroutines.tasks.await

suspend fun removeBackground(context: Context, uri: Uri): Bitmap? {

    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null

    val image = InputImage.fromBitmap(bitmap, 0)

    val options = SelfieSegmenterOptions.Builder()
        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
        .enableRawSizeMask()
        .build()

    val segmenter = Segmentation.getClient(options)

    val result = segmenter.process(image).await()

    val mask = result.buffer
    val width = result.width
    val height = result.height

    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    mask.rewind()

    for (y in 0 until height) {
        for (x in 0 until width) {

            val confidence = mask.float

            if (confidence > 0.5f) {
                output.setPixel(x, y, bitmap.getPixel(x, y))
            } else {
                output.setPixel(x, y, Color.TRANSPARENT)
            }
        }
    }

    return output
}