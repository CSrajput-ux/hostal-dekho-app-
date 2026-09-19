package com.company.hostaldekho.util

import android.util.Patterns

object Validation {

    fun isValidUsername(name: String): Boolean {
        return name.trim().length >= 3
    }

    fun isValidRealName(name: String): Boolean {
        return name.isNotEmpty() && name.all { it.isLetter() || it.isWhitespace() }
    }

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Password Requirements:
     * - Minimum 8 characters
     * - At least one uppercase
     * - At least one lowercase
     * - At least one number
     * - At least one special character
     */
    fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    fun doPasswordsMatch(pass: String, confirmPass: String): Boolean {
        return pass == confirmPass
    }
}
