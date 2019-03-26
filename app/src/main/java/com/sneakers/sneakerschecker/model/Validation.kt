package com.sneakers.sneakerschecker.model

import android.util.Patterns;
import java.util.regex.Pattern


class Validation {
    companion object {

        private val PASSWORD_PATTERN = Pattern.compile(
            "^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter

                    "(?=.*[@#$%^&+=])" +    //at least 1 special character

                    "(?=\\S+$)" +           //no white spaces

                    ".{6,}" +               //at least 4 characters

                    "$"
        )

        fun validateEmail(email: String): Boolean? {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return true
            }
            return false
        }

        fun validatePassword(password: String): Boolean? {
            if (PASSWORD_PATTERN.matcher(password).matches()) {
                return true
            }
            return false
        }
    }
}