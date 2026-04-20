package com.toylibrary.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.MobileAds
import com.toylibrary.app.ui.theme.MainScreen
import com.toylibrary.app.auth.AuthScreen
import com.toylibrary.app.auth.isUserLoggedIn

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        println("API KEY = ${com.toylibrary.app.BuildConfig.GEMINI_API_KEY}")

        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        enableEdgeToEdge()

        setContent {

            val context = LocalContext.current

            var isLoggedIn by remember {
                mutableStateOf(isUserLoggedIn(context))
            }

            var showLoginScreen by remember {
                mutableStateOf(false)
            }

            if (showLoginScreen) {

                AuthScreen(
                    onLoginSuccess = {
                        isLoggedIn = true
                        showLoginScreen = false
                    }
                )

            } else {

                MainScreen(
                    isLoggedIn = isLoggedIn,
                    onLoginRequired = {
                        showLoginScreen = true
                    }
                )
            }
        }
    }
}