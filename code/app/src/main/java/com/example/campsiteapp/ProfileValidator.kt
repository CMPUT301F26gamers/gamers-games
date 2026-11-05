package com.example.campsiteapp

class ProfileValidator {

    fun isValidName(name: String): Boolean {
        return name.isNotBlank()
    }

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.contains(" ")) {
            return false
        }

        val parts = trimmed.split("@")
        if (parts.size != 2) {
            return false
        }

        val user = parts[0]
        val domain = parts[1]
        if (user.isEmpty() || domain.isEmpty()) {
            return false
        }
        if (!domain.contains(".") || domain.startsWith(".")) {
            return false
        }

        return domain.substringAfterLast(".").length >= 2
    }

    fun isValidPhone(phone: String): Boolean {
        val trimmed = phone.trim()
        if (trimmed.isEmpty()) {
            return true
        }

        var digits = 0
        for (i in trimmed.indices) {
            val char = trimmed[i]
            if (char.isDigit()) {
                digits += 1
            } else if (char == '+' && i == 0) {
                continue
            } else if (!"-() ".contains(char)) {
                return false
            }
        }

        return digits in 10..15
    }
}
