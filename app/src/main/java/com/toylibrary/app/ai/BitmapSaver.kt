package com.toylibrary.app.ai

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun saveBitmapAsPng(context: Context, bitmap: Bitmap): Uri {

    val file = File(
        context.cacheDir,
        "toy_${System.currentTimeMillis()}.png"
    )

    FileOutputStream(file).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    }

    return Uri.fromFile(file)
}