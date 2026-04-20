package com.toylibrary.app.auth

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow

import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.toylibrary.app.R

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.ExperimentalComposeUiApi

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {

    val context = LocalContext.current

    val autofill = LocalAutofill.current
    val autofillTree = LocalAutofillTree.current

    val nameFocusRequester = remember { FocusRequester() }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }

    LaunchedEffect(isLogin) {
        if (!isLogin) {
            nameFocusRequester.requestFocus()
        }
    }
    val emailNode = AutofillNode(
        autofillTypes = listOf(AutofillType.EmailAddress),
        onFill = { email = it }
    )

    val passwordNode = AutofillNode(
        autofillTypes = listOf(AutofillType.Password),
        onFill = { password = it }
    )

    autofillTree += emailNode
    autofillTree += passwordNode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()   // pushes content above keyboard
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🧸 ToyLibrary Logo
            Image(
                painter = painterResource(R.drawable.toylibrary_logo),
                contentDescription = "ToyLibrary Logo",
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "ToyLibrary",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isLogin) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocusRequester)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        emailNode.boundingBox = it.boundsInWindow()
                    }
                    .onFocusChanged {
                        if (it.isFocused) autofill?.requestAutofillForNode(emailNode)
                        else autofill?.cancelAutofillForNode(emailNode)
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        passwordNode.boundingBox = it.boundsInWindow()
                    }
                    .onFocusChanged {
                        if (it.isFocused) autofill?.requestAutofillForNode(passwordNode)
                        else autofill?.cancelAutofillForNode(passwordNode)
                    }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {

                        saveLoginState(context, true)

                        if (!isLogin) {  // only save name during sign up
                            saveUserInfo(context, name, email)
                        }

                        onLoginSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLogin) "Login" else "Sign Up")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { isLogin = !isLogin }
            ) {
                Text(
                    if (isLogin)
                        "Don't have an account? Sign Up"
                    else
                        "Already have an account? Login"
                )
            }
        }
    }
}

fun getUserName(context: Context): String {
    val prefs = context.getSharedPreferences("toy_library_prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_name", "User") ?: "User"
}

fun getUserEmail(context: Context): String {
    val prefs = context.getSharedPreferences("toy_library_prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_email", "") ?: ""
}

fun saveUserInfo(context: Context, name: String, email: String) {
    val prefs = context.getSharedPreferences("toy_library_prefs", Context.MODE_PRIVATE)
    prefs.edit()
        .putString("user_name", name)
        .putString("user_email", email)
        .apply()
}
fun saveLoginState(context: Context, isLoggedIn: Boolean) {
    val prefs = context.getSharedPreferences("toy_library_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("is_logged_in", isLoggedIn).apply()
}
fun isUserLoggedIn(context: Context): Boolean {
    val prefs = context.getSharedPreferences("toy_library_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("is_logged_in", false)
}