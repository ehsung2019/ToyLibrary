package com.toylibrary.app.profile

import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.toylibrary.app.auth.saveLoginState
import com.toylibrary.app.util.formatEmail
import com.toylibrary.app.billing.BillingManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ProfileScreen(
    onUpgradeClick: () -> Unit,
    onLogout: () -> Unit,
    billingManager: BillingManager   // ✅ ADD THIS
) {
    val context = LocalContext.current

    val name = getUserName(context)
    val email = getUserEmail(context)
    var isSubscribed by remember {
        mutableStateOf(isUserSubscribed(context))
    }

    LaunchedEffect(Unit) {
        billingManager.startConnection {
            billingManager.queryExistingPurchases { isPremium ->
                isSubscribed = isPremium
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = formatEmail(email),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ⭐ Subscription section
        if (isSubscribed) {
            ProfileItem(
                title = "Premium Member ⭐",
                highlight = true
            )
        } else {
            ProfileItem(
                title = "Upgrade to Premium ⭐",
                onClick = {
                    saveSubscription(context, true) // TEMP for testing
                    onUpgradeClick()
                },
                highlight = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ProfileItem("My Loans")
        ProfileItem("Settings")
        ProfileItem("Help & Support")

        ProfileItem(
            title = "Log out",
            isDestructive = true,
            onClick = {
                saveLoginState(context, false)
                onLogout()
            }
        )
    }
}

@Composable
fun ProfileItem(
    title: String,
    onClick: (() -> Unit)? = null,
    isDestructive: Boolean = false,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    isDestructive -> Color.Red
                    highlight -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

fun isUserSubscribed(context: Context): Boolean {
    val prefs = context.getSharedPreferences("toy_library_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("is_subscribed", false)
}

fun saveSubscription(context: Context, subscribed: Boolean) {
    val prefs = context.getSharedPreferences("toy_library_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("is_subscribed", subscribed).apply()
}
fun getUserName(context: Context): String {
    val prefs = context.getSharedPreferences("toy_library_prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_name", "User") ?: "User"
}

fun getUserEmail(context: Context): String {
    val prefs = context.getSharedPreferences("toy_library_prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_email", "") ?: ""
}

fun createPurchasesUpdatedListener(context: Context): PurchasesUpdatedListener {

    return PurchasesUpdatedListener { billingResult, purchases ->

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {

            for (purchase in purchases) {

                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

                    saveSubscription(context, true)

                }
            }
        }
    }
}