package com.toylibrary.app.data

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage

data class UserToy(
    val name: String,
    val age: String,
    val description: String,
    val owner: String,
    val imageUri: String? = null
)