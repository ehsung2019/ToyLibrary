package com.toylibrary.app.util

fun formatEmail(email: String): String {
    val parts = email.split("@")
    if (parts.size != 2) return email

    val name = parts[0]
    val domain = parts[1]

    val maskedName = if (name.length <= 2) {
        "*".repeat(name.length)
    } else {
        name.first() + "*".repeat(name.length - 2) + name.last()
    }

    return "$maskedName@$domain"
}