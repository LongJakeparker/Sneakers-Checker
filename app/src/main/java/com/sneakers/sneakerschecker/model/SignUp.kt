package com.sneakers.sneakerschecker.model

import com.google.gson.annotations.SerializedName

class SignUp {
    @SerializedName("passwordHash")
    var passwordHash: String

    constructor(passwordHash: String) {
        this.passwordHash = passwordHash
    }
}