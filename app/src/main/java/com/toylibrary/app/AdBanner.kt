// AdBanner.kt
package com.toylibrary.app.ui.theme

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp

import androidx.compose.ui.graphics.Color

@Composable
fun AdBanner() {
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)

                // 🔥 USE TEST ID FIRST
                adUnitId = "ca-app-pub-3940256099942544~6300978111"

                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Red)
    )
}